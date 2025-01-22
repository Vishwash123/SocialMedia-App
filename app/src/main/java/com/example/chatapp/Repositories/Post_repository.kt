package com.example.chatapp.Repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.chatapp.Models.Post
import com.example.chatapp.Models.PostType
import com.example.chatapp.Utilities.AttachmentUtils
import com.example.chatapp.Utilities.AuthUtils
import com.example.chatapp.Utilities.CloudinaryHelper
import com.example.chatapp.Utilities.FirebaseService
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.getValue
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Post_repository @Inject constructor(){
    val database = FirebaseService.firebaseDatabase.reference

    fun createPost(context: Context,userId:String, uris:List<Uri>, caption:String,postType: PostType,onProgress:(Int)->Unit,onComplete:()->Unit,onFailed:()->Unit){
//        Log.d("posts repo","insidne the function ")
//        val postId = database.child("Posts").push().key?:return
//        Log.d("posts repo","$postId")
//        var profilePicUrl = ""
//        getUserProfilePic(userId, onResult = {url->
//            profilePicUrl = url
//        },onIssue={
//
//        })
//        if(profilePicUrl==""){
//            Log.d("posts repo","empty profile pic")
//            onFailed()
//            return
//        }
//        Log.d("posts repo","$profilePicUrl")
//        val post = Post(postId = postId, posterId = userId, posterProfilePic = profilePicUrl!!, timestamp = System.currentTimeMillis(),
//            likes = 0, comments = 0,slides=uris.size, caption = caption, postType = postType)
//
//        val urlList = mutableListOf<String>()
//        fun handleUploadError() {
//            // Delete the partially created post from Firebase
//            database.child("Posts/$postId").removeValue()
//
//            // Stop the function execution
//            return
//        }
//        var uploadCount = 0
//        for(i in uris.indices){
//
//            val filePath = AuthUtils.getRealPathFromURI(context, uris[i])
//            val fileType = AttachmentUtils.getFileTypeFromUri(uris[i],filePath,context)
//            CloudinaryHelper.uploadFile(
//                filePath = filePath!!,
//                fileType = fileType,
//                onStart = {
//                    onProgress(uris.size*100)
//                },
//                onProgress = {progress->
//                    onProgress(progress)
//                },
//                onSuccess = {url->
//                    urlList.add(url)
//                    uploadCount++
//                    if(uploadCount==uris.size){
//                        post.mediaUrls = urlList
//                        database.child("Posts/$postId").setValue(post)
//                        onComplete()
//                    }
//                },
//                onError = {
//                    handleUploadError()
//                    onFailed()
//                }
//            )
//        }


        Log.d("posts repo", "inside the function")
        val postId = database.child("Posts").push().key ?: return
        Log.d("posts repo", "$postId")

        getUserProfilePic(userId, onResult = { profilePicUrl ->
            Log.d("posts repo", "Profile pic URL: $profilePicUrl")
            val post = Post(
                postId = postId,
                posterId = userId,
                posterProfilePic = profilePicUrl,
                timestamp = System.currentTimeMillis(),
                likes = 0,
                comments = 0,
                slides = uris.size,
                caption = caption,
                postType = postType
            )

            val urlList = mutableListOf<String>()
            var uploadCount = 0
            var c = 1

            fun handleUploadError() {
                database.child("Posts/$postId").removeValue()
                onFailed()
            }

            for (i in uris.indices) {
                val filePath = AuthUtils.getRealPathFromURI(context, uris[i])
                val fileType = AttachmentUtils.getFileTypeFromUri(uris[i], filePath, context)

                CloudinaryHelper.uploadFile(
                    filePath = filePath!!,
                    fileType = fileType,
                    onStart = {
                        onProgress(c)
                    },
                    onProgress = { progress ->

                        Log.d("new post progress repo","$progress")

                        if(progress==100){
                            c++
                            onProgress(c)
                        }
                    },
                    onSuccess = { url ->
                        urlList.add(url)

                        uploadCount++

                        if (uploadCount == uris.size) {
                            post.mediaUrls = urlList
                            database.child("Posts/$postId").setValue(post)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val userPostsRef = database.child("users/$userId").child("posts")
                                        userPostsRef.runTransaction(object: Transaction.Handler {
                                            override fun doTransaction(currentData: MutableData): Transaction.Result {
                                                val currentList = currentData.getValue<List<String>>() ?: emptyList()

                                                if (currentList.contains(postId)) {
                                                    // If it exists, don't update
                                                    return Transaction.success(currentData)
                                                }

                                                val updatedList = currentList + postId
                                                currentData.value = updatedList
                                                return Transaction.success(currentData)
                                            }

                                            override fun onComplete(
                                                error: DatabaseError?,
                                                committed: Boolean,
                                                currentData: DataSnapshot?
                                            ) {

                                                onComplete()
                                            }
                                        })
                                    } else {
                                        handleUploadError()
                                    }
                                }
                        }
                    },
                    onError = {
                        handleUploadError()
                    }
                )
            }
        }, onIssue = {
            Log.d("posts repo", "Failed to fetch profile pic")
            onFailed()
        })

    }

    private fun getUserProfilePic(userId: String, onResult: (String) -> Unit, onIssue:()->Unit) {

        database.child("users/$userId").child("profilePic").get().addOnSuccessListener { snapshot->
            if(snapshot.exists()){
                val url = snapshot.value.toString()
                onResult(url)
            }else{
                onIssue()
            }

        }.addOnFailureListener{
            Log.d("posts xxo","failed to fetch profile pic")
            onIssue()
        }

    }
}