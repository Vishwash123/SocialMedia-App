package com.example.chatapp.Repositories

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.chatapp.Models.Comment
import com.example.chatapp.Models.CommenterData
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
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@Singleton
class Post_repository @Inject constructor(){
    val database = FirebaseService.firebaseDatabase.reference
    private val TAG = "POSTFUN"
    private val TAG2 = "COMMENTS FETCHING XXXO"
    private val TAG3 = "REPLIES FETCHING XXXO"

    fun createPost(context: Context,userId:String, uris:List<Uri>, caption:String,postType: PostType,userName:String,onProgress:(Int)->Unit,onComplete:()->Unit,onFailed:()->Unit){


        Log.d("posts repo", "inside the function")
        val postId = database.child("Posts").child(userId).push().key ?: return
        Log.d("posts repo", "$postId")

        getUserProfilePic(userId, onResult = { profilePicUrl ->
            Log.d("posts repo", "Profile pic URL: $profilePicUrl")
            val post = Post(
                postId = postId,
                posterId = userId,
                posterName = userName,
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
                            database.child("Posts/$userId/$postId").setValue(post)
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


    fun fetchUserPosts(userId: String,onResult: (List<Post>) -> Unit){
        Log.d("profile vm xxxo","inside repo")

        val ref = database.child("Posts/$userId")
        ref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val postList = mutableListOf<Post>()
                var postCount = snapshot.childrenCount.toInt()
                Log.d("profile vm xxxo","inside data change")
                if(snapshot.exists()){
                    Log.d("profile vm xxxo","inside snapshot starting loop")
                    for (postSnapshot in snapshot.children) {


                        val post = postSnapshot.getValue(Post::class.java)
                        Log.d("profile vm xxxo","$post")

                        post?.let {
                            postList.add(it)
                        }
                        postCount--
                        if (postCount == 0) {
                            onResult(postList)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }

        })
    }

    suspend fun handleLike(userId:String,postId:String,like:Boolean){
        val ref = database.child("Posts/$userId/$postId").child("likes")
        ref.runTransaction(object :Transaction.Handler{
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentLikes = currentData.getValue(Int::class.java)?:0
                if(like) {
                    currentData.value = currentLikes + 1
                }
                else{
                    if(currentLikes>0) currentData.value = currentLikes - 1
                }
                Log.d("likes xxxo","likes handled")
                return Transaction.success(currentData)

            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if(error!=null){
                    Log.d("likes xxxo","ERROR")

                }

                handleLikers(userId,postId,like)

            }

        })
    }


    private fun handleLikers(
        userId: String,
        postId: String,
        like: Boolean
        //onResult: (Boolean) -> Unit
    ){
        Log.d("likes xxxo","likers handled")
        val likersref = database.child("Posts/$userId/$postId").child("likers")
        likersref.runTransaction(object :Transaction.Handler{
            @SuppressLint("SuspiciousIndentation")
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                var currentList= currentData.getValue<List<String>>() ?: emptyList()
                val currentUserId = FirebaseService.firebaseAuth.currentUser!!.uid
                if(like){
                    currentList = currentList + FirebaseService.firebaseAuth.currentUser!!.uid
                }
                else{

                    if(currentList.contains(currentUserId)) currentList = currentList - currentUserId
                }

                currentData.value = currentList
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
               if(error!=null){
                   Log.d("likes xxxo","likers handled")
                  // onResult(false)
               }
//                else{
//                   onResult(true)
//               }



            }

        })
    }


    suspend fun addComment(commentId:String,context: Context,userId: String,postId: String,imageUri: Uri?,text:String){
        //val commentId = database.child("Comments").child("$postId/comments").push().key?:return
        val comment = Comment(commentId,userId,postId,System.currentTimeMillis(), mutableListOf(),text, replies = mutableListOf())
        if(imageUri!=null){
            val filePath = AuthUtils.getRealPathFromURI(context,imageUri)
            val fileType = AttachmentUtils.getFileTypeFromUri(imageUri,filePath,context)
            comment.imageUrl = withContext(Dispatchers.IO){
                suspendCoroutine { continuation ->
                    CloudinaryHelper.uploadFile(
                        filePath!!,
                        fileType,
                        onStart = {

                        },
                        onProgress = {

                        },
                        onSuccess = {url->
                            continuation.resume(url)
                        },
                        onError = {
                            continuation.resume(null)
                        }
                    )
                }
            }

        }



        val commentRef = database.child("Comments").child(postId).child("comments")
        commentRef.runTransaction(object :Transaction.Handler{
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val comments = currentData.value as? MutableMap<String,Any>?: mutableMapOf()
                comments[commentId]=comment
                currentData.value=comments
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
               if(error==null){
                        CoroutineScope(Dispatchers.IO).launch {
                            addCommentIdToPost(commentId,postId,userId)
                        }




               }
                else{
                    Log.d(TAG,"error adding comment")
               }
            }

        })

    }

    private suspend fun addCommentIdToPost(commentId: String, postId: String, userId: String) {
        val ref = database.child("Posts/$userId/$postId/commentIds")

        withContext(Dispatchers.IO){
            ref.runTransaction(object :Transaction.Handler{
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val currentList = currentData.getValue<List<String>>()?: emptyList()
                    val updatedList = currentList + commentId
                    currentData.value = updatedList
                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if(error==null){
                        Log.d(TAG,"Comment added to Post successfully")
                        database.child("Posts/$userId/$postId/comments").runTransaction(object :Transaction.Handler{
                            override fun doTransaction(currentData: MutableData): Transaction.Result {
                                var currentCommentCount = currentData.getValue<Int>()
                                val newCount = currentCommentCount?.plus(1)
                                currentData.value = newCount
                                return Transaction.success(currentData)
                            }

                            override fun onComplete(
                                error: DatabaseError?,
                                committed: Boolean,
                                currentData: DataSnapshot?
                            ) {
                                Log.d(TAG,"Comment added to Post successfully")
                            }

                        })
                    }
                    else{
                        Log.d(TAG,"Failed to add comment to post")
                    }

                }

            })

        }

    }


    suspend fun replyToComment(replyId:String,context: Context,postId: String,userId:String,text:String,imageUri:Uri?,commentId: String){
       // val replyId = database.child("Comments/$postId").child("replies").push().key?:return
        Log.d("Replying to comment repo xxo","in")
        val replyRef = database.child("Comments").child(postId).child("replies")
        val reply = Comment(
            replyId,
            userId,
            postId,
            System.currentTimeMillis(),
            mutableListOf(),
            text,
            replies = mutableListOf()
        )
        Log.d("Replying to comment repo xxo"," before uploading image")
        if(imageUri!=null) {
            Log.d("Replying to comment repo xxo", "uploading image")
            val filePath = AuthUtils.getRealPathFromURI(context, imageUri)
            val fileType = AttachmentUtils.getFileTypeFromUri(imageUri, filePath, context)
            reply.imageUrl = withContext(Dispatchers.IO) {
                suspendCoroutine { continuation ->
                    CloudinaryHelper.uploadFile(
                        filePath!!,
                        fileType,
                        onStart = {

                        },
                        onProgress = {

                        },
                        onError = {
                            continuation.resume(null)
                        },
                        onSuccess = { url ->
                            continuation.resume(url)
                        }
                    )
                }
            }
        }

            Log.d("Replying to comment repo xxo","after uploading image")

            replyRef.runTransaction(object :Transaction.Handler{
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val replies = currentData.value as? MutableMap<String,Comment> ?: mutableMapOf()
                    replies[replyId] = reply
                    currentData.value = replies
                    Log.d("Replying to comment repo xxo","inside transaction new size ${replies.size}")
                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {

                    if(error==null){
                        addReplyToComment(replyId,commentId,postId)
                    }
                    else{
                        Log.d("Replying to comment repo xxo","Failed to create reply")
                    }

                }

            })

    }

    private fun addReplyToComment(replyId: String, commentId: String, postId: String) {
        Log.d("Replying to comment repo xxo","adding reply id to comment")
        val ref = database.child("Comments").child(postId).child("comments").child(commentId).child("replies")
        ref.runTransaction(object :Transaction.Handler{
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentReplies = currentData.getValue<List<String>>()?: emptyList()
                val updatedReplies = currentReplies + replyId
                currentData.value = updatedReplies
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if(error==null){
                    Log.d(TAG,"reply added to comment successfully")
                }
                else{
                    Log.d(TAG,"Failed to add reply to comment")
                }
            }

        })
    }

    suspend fun handleCommentUpdates(context: Context,userId: String,postId:String,commentId: String,like:Boolean?,replies:List<Comment>?){

        if(like!=null) {
            val ref = database.child("Comments/$postId/comments/$commentId").child("likes")
            ref.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    var currentList = currentData.getValue<List<String>>() ?: emptyList()

                    if (like) {
                        currentList = currentList + FirebaseService.firebaseAuth.currentUser!!.uid
                    } else {
                        currentList = currentList - FirebaseService.firebaseAuth.currentUser!!.uid
                    }

                    currentData.value = currentList
                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (error != null) {
                        Log.d("likes xxxo", "likers handled")
                        // onResult(false)
                    }
//                else{
//                   onResult(true)
//               }


                }

            })
        }

        if(replies!=null){
            replies.forEach{reply->
                //note image url here will represent the uri currently
                //the reply to comment will automaticallyy create a new comment which will cotnain the url

                Log.d("reply info xxo","${reply.imageUri}")
                replyToComment(reply.commentId,context,postId,reply.userId,reply.text,imageUri = reply.imageUri,commentId)
            }
        }
    }


    suspend fun fetchComments(postId: String):Flow<List<Comment>> = callbackFlow{
        Log.d(TAG2,"Started fetching")
        val ref = database.child("Comments").child(postId).child("comments")
        val listener = object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val commentList = mutableListOf<Comment>()

                var commentCount = snapshot.childrenCount.toInt()
                Log.d(TAG2,"to fetch ${commentCount}")

                if(snapshot.exists()){
                    Log.d(TAG2,"GOT SNAPSHOT ")
                    for(commentSnapshot in snapshot.children){
                        val comment = commentSnapshot.getValue(Comment::class.java)
                        comment?.let {
                            commentList.add(it)
                            commentCount--
                        }

                        Log.d(TAG2,"remaining ${commentCount}")
                        if(commentCount==0){

                            trySend(commentList)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }

        }
        ref.addValueEventListener(listener)
        awaitClose{ref.removeEventListener(listener)}
    }


    suspend fun fetchReplies(postId:String):Flow<List<Comment>> = callbackFlow{
        Log.d(TAG3,"inside fetching")
        val ref = database.child("Comments").child(postId).child("replies")
        val listener = object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val replyList = mutableListOf<Comment>()
                var replyCount = snapshot.childrenCount.toInt()

                if(snapshot.exists()){

                    for(replySnapshot in snapshot.children){
                        val reply = replySnapshot.getValue(Comment::class.java)
                        reply?.let {
                            replyList.add(it)
                        }
                        replyCount--
                        if(replyCount==0){
                            trySend(replyList)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }

        }

        ref.addValueEventListener(listener)
        awaitClose{ref.removeEventListener(listener)}

    }


    fun fetchCommenterData(userId:String,onResult: (CommenterData?) -> Unit){
         val ref = database.child("users").child(userId)
        ref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.child("name").getValue(String::class.java)
                val profilePic = snapshot.child("profilePic").getValue(String::class.java)

                Log.d("Fetching commenter data xxxo","$userId and $userName and $profilePic")
                val commenter = CommenterData(userId,userName!!,profilePic!!)
                onResult(commenter)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(null)
            }

        })
    }






}