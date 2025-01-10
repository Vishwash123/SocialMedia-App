package com.example.chatapp.Repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.chatapp.Models.User
import com.example.chatapp.Utilities.AuthUtils
import com.example.chatapp.Utilities.CloudinaryHelper
import com.example.chatapp.Utilities.FirebaseService
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope

import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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


    suspend fun signOut(){
        auth.signOut()
    }
}