package com.example.chatapp.Repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.chatapp.Models.ProfileUiState
import com.example.chatapp.Models.User
import com.example.chatapp.Utilities.AuthUtils
import com.example.chatapp.Utilities.CloudinaryHelper
import com.example.chatapp.Utilities.FirebaseService
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class User_Repository @Inject constructor() {
    private val auth = FirebaseService.firebaseAuth
    private val storage = FirebaseService.firebaseStorage
    private val database:DatabaseReference = FirebaseService.firebaseDatabase.reference


    suspend fun signUp(
        email: String,
        password: String,
        user: User,
        uri: Uri?,
        context: Context,
        onProgress: (Int) -> Unit,
        onComplete: (Result<FirebaseUser?>) -> Unit
    ) {
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed.")

            uri?.let { imageUri ->
                val filePath = AuthUtils.getRealPathFromURI(context, imageUri)
                if (!filePath.isNullOrEmpty() && File(filePath).exists()) {
                    CloudinaryHelper.uploadFile(
                        filePath = filePath,
                        fileType = "image",
                        onStart = { onProgress(0) },
                        onProgress = { progress -> onProgress(progress) },
                        onError = { error -> onComplete(Result.failure(Exception(error))) },
                        onSuccess = { url ->
                            user.profilePic = url
                            user.id = firebaseUser.uid
                            CoroutineScope(Dispatchers.IO).launch {
                                updateFirebaseProfile(firebaseUser, user, onComplete)
                            }
                        }
                    )
                } else {
                    user.id = firebaseUser.uid
                    updateFirebaseProfile(firebaseUser, user, onComplete)
                }
            } ?: run {
                user.id = firebaseUser.uid
                updateFirebaseProfile(firebaseUser, user, onComplete)
            }
        } catch (e: Exception) {
            Log.e("User_Repository", "Error during sign-up: ${e.message}")
            onComplete(Result.failure(e))
        }
    }

    private suspend fun updateFirebaseProfile(
        firebaseUser: FirebaseUser,
        user: User,
        onComplete: (Result<FirebaseUser?>) -> Unit
    ) {
        try {
            // Update Firebase Auth profile
            val profileChangeRequest = UserProfileChangeRequest.Builder()
                .setDisplayName(user.name)
                .setPhotoUri(Uri.parse(user.profilePic))
                .build()

            firebaseUser.updateProfile(profileChangeRequest).await()

            // Save user to Firebase Realtime Database
            user.posts = user.posts.ifEmpty { listOf() }
            user.friends = user.friends.ifEmpty { mapOf() }
            user.sentFriendRequests = user.sentFriendRequests.ifEmpty { mapOf() }
            user.receivedFriendRequests = user.receivedFriendRequests.ifEmpty { mapOf() }
            user.chats = user.chats.ifEmpty { listOf() }

            database.child("users").child(firebaseUser.uid).setValue(user).await()
            onComplete(Result.success(firebaseUser))
        } catch (e: Exception) {
            Log.e("User_Repository", "Error updating profile or saving user data: ${e.message}")
            onComplete(Result.failure(e))
        }
    }




    suspend fun logIn(email: String,password: String):Result<FirebaseUser?>{
        return try{
            val result = auth.signInWithEmailAndPassword(email,password).await()
            Result.success(result.user)
        }catch(e:Exception){
            Result.failure(e)
        }
    }



    suspend fun fetchUser(uid:String):Result<User?>{
        return try{
            val snapshot = database.child("users").child(uid).get().await()
            val user = snapshot.getValue(User::class.java)
            Result.success(user)
        }catch(e:Exception){
            Result.failure(e)
        }
    }


  //  fun getUserDetails(uid: String):User?{
//
//            val snapshot = database.child("users").child(uid).get().addOnSuccessListener { snapshot->
//
//                val user = snapshot.getValue(User::class.java)
//                Log.d("call vm xxo","${user.toString()}")
//                return@addOnSuccessListener
//
//            }
//
//
//
//
//    }
  fun getUserDetails(userId: String, callback: (User?) -> Unit) {
      val userRef = database.child("users").child(userId)
      userRef.addListenerForSingleValueEvent(object : ValueEventListener {
          override fun onDataChange(snapshot: DataSnapshot) {
              val user = snapshot.getValue(User::class.java)
              callback(user) // Return the user data through the callback
          }

          override fun onCancelled(error: DatabaseError) {
              callback(null) // If there's an error, return null
          }
      })
  }

    // Fetch the list of call IDs associated with a user
    fun getUserCalls(userId: String, callback: (List<String>) -> Unit) {
        val userCallsRef = database.child("users").child(userId).child("callIds")
        userCallsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val calls = mutableListOf<String>()
                snapshot.children.forEach {
                    it.getValue(String::class.java)?.let { callId ->
                        calls.add(callId)
                    }
                }
                callback(calls) // Return the list of call IDs through the callback
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList()) // If there's an error, return an empty list
            }
        })
    }

    suspend fun getUsersDataForIds(ids: List<String>): List<User> {
        val users = mutableListOf<User>()

        try {
            // Loop through each ID and fetch user data
            val usersRef = database.child("users")
            ids.forEach { userId ->
                val userSnapshot = usersRef.child(userId).get().await() // Suspend until data is fetched

                val user = userSnapshot.getValue(User::class.java)
                user?.let { users.add(it) }
            }
        } catch (e: Exception) {
            // Handle any errors that occur during fetching data
            e.printStackTrace()
        }

        return users
    }

    suspend fun signOut(){
        auth.signOut()
    }

    fun fetchProfileUiState(userId:String,onResult:(ProfileUiState)->Unit){
        val ref = database.child("users/$userId")
        val profileUiState = ProfileUiState()
        ref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue(User::class.java)
                    profileUiState.name = user!!.name
                    profileUiState.profilePic = user.profilePic
                    profileUiState.posts = user.posts.size
                    profileUiState.friends = user.friends.size
                    profileUiState.bio = user.bio
                    onResult(profileUiState)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


    }


    fun updateBio(userId:String,bio:String){
        val ref = database.child("users/$userId").child("bio").setValue(bio)


    }

    fun fetchAndReturnUserData(userId: String,onResult: (ProfileUiState) -> Unit){
        val ref = database.child("users/$userId")
        val otherUserState= ProfileUiState()
        ref.get().addOnSuccessListener {snapshot->
            if(snapshot!=null){
                val user = snapshot.getValue(User::class.java)
                otherUserState.name = user!!.name
                otherUserState.bio = user!!.bio
                otherUserState.friends = snapshot.child("friends").childrenCount.toInt()
                otherUserState.profilePic = user!!.profilePic
                val postsSnapshot = snapshot.child("posts")
                val postIdList = mutableListOf<String>()
                if(postsSnapshot.exists()){
                    postsSnapshot.children.forEach{
                        postIdList.add(it.getValue(String::class.java)?:"")
                    }
                    otherUserState.postIds = postIdList
                    otherUserState.posts = postIdList.size
                }

            }
            onResult(otherUserState)

        }.addOnFailureListener{
            onResult(ProfileUiState())
        }
    }


    fun updateProfilePhoto(userId: String, uri:Uri?, context: Context, onProgress: (Int) -> Unit, onComplete:()->Unit){
        val ref = database.child("users/$userId")
        uri?.let { imageUri ->
            val filePath = AuthUtils.getRealPathFromURI(context, imageUri)
            if (!filePath.isNullOrEmpty() && File(filePath).exists()) {
                CloudinaryHelper.uploadFile(
                    filePath = filePath,
                    fileType = "image",
                    onStart = { onProgress(0) },
                    onProgress = { progress -> onProgress(progress) },
                    onError = { error -> onComplete() },
                    onSuccess = { url ->
                        ref.child("profilePic").setValue(url)
                        CoroutineScope(Dispatchers.IO).launch {
                            val profileChangeRequest = UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse(url)).build()
                            FirebaseService.firebaseAuth.currentUser!!.updateProfile(profileChangeRequest).await()
                            onComplete()
                        }
                    }
                )
            } else {


            }
        } ?: run {

            //go after
        }

    }
}