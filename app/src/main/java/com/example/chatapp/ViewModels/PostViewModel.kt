package com.example.chatapp.ViewModels

import android.content.Context
import android.net.Uri
import android.util.Dumpable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.Models.Comment
import com.example.chatapp.Models.CommentUpdates
import com.example.chatapp.Models.CommenterData
import com.example.chatapp.Models.Post
import com.example.chatapp.Models.PostType
import com.example.chatapp.Repositories.Post_repository
import com.example.chatapp.Repositories.User_Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: Post_repository
):ViewModel(){

    private val _postUploadProgress = MutableLiveData<Int?>()
    val postUploadProgress: LiveData<Int?> = _postUploadProgress

    private val _postUploadSuccess = MutableLiveData<Boolean>()
    val postUploadSuccess:LiveData<Boolean> = _postUploadSuccess

    private val _userPostList = MutableLiveData<List<Post>?>()
    val userPostList:LiveData<List<Post>?> = _userPostList

    private val _commentsList = MutableStateFlow<List<Comment>>(emptyList())
    val commentsList: StateFlow<List<Comment>> = _commentsList

    private val _repliesList = MutableStateFlow<Map<String,Comment>>(mutableMapOf())
    val repliesList: StateFlow<Map<String,Comment>> = _repliesList

    private val _commentersList = MutableStateFlow<Map<String,CommenterData>>(mutableMapOf())
    val commentersList: StateFlow<Map<String,CommenterData>> = _commentersList

    private val pendingComments = mutableListOf<Comment>()
    private val pendingLikeUpdates = mutableMapOf<String,Boolean>()
    private val pendingCommentUpdates = mutableMapOf<String,CommentUpdates>()

//    private val updatingPosts = mutableSetOf<String>() // Tracks posts being updated
//    private val pendingToggles = mutableMapOf<String, Boolean>() // Tracks latest toggle for posts


    fun createPost(
        context: Context,
        userId: String,
        uris: List<Uri>,
        caption: String,
        postType: PostType,
        displayName: String?
    ){


        Log.d("new posts observer xxo vm","set to ${_postUploadProgress.value}")
        postRepository.createPost(context,userId,uris,caption,postType,displayName!!,
            onProgress = {progress->
//                 remainingProgress -= progress
                _postUploadProgress.postValue(progress)
            },
            onComplete = {
                _postUploadSuccess.postValue(true)
                _postUploadProgress.postValue(-1)
            },
            onFailed = {
                _postUploadSuccess.postValue(false)
                _postUploadProgress.postValue(-1)
            })
    }


    fun fetchUserPosts(userId: String){
        Log.d("profile vml xxxo","inside vm")
            postRepository.fetchUserPosts(userId){list->
                Log.d("profile vm xxxo","${list.size}")
                _userPostList.postValue(list)
            }



    }

//    fun toggleLike(userId:String,postId:String,like:Boolean){
//        //remove the like parameter change based on whether the value is  present or not in the likers list
//        val posts = _userPostList.value?.toMutableList()
//        val post = posts?.find{it.postId==postId}
//        post?.let{
//            if(like){
//                post.likers.add(userId)
//                post.likes+=1
//            }
//            else{
//                post.likers.remove(userId)
//                post.likes-=1
//            }
//        }
//        _userPostList.value = posts
//
//        if(updatingPosts.contains(postId)){
//            pendingToggles[postId] = like
//            return
//        }
//        addLike(userId,postId,like)
//    }



//    fun addLike(userId:String,postId:String,like:Boolean){
//        updatingPosts.add(postId)
//
//        postRepository.handleLike(userId,postId,like){success->
//            if(success) {
//                updatingPosts.remove(postId)
//                if (pendingToggles.contains(postId)) {
//                    val latestState = pendingToggles.remove(postId)
//                    addLike(userId, postId, like)
//                }
//            }
//            else{
//                val posts = _userPostList.value?.toMutableList()
//                val post = posts?.find{it.postId == postId}
//                post?.let{
//                    if(like){
//                        it.likers.remove(userId)
//                        it.likes-=1
//                    }
//                    else{
//                        it.likers.add(userId)
//                        it.likes+=1
//                    }
//
//                }
//                _userPostList.value = posts
//                updatingPosts.remove(postId)
//
//            }
//        }
//    }

    suspend fun addLike(postId:String,userId: String,like:Boolean){
        GlobalScope.launch(Dispatchers.IO){
            postRepository.handleLike(userId,postId,like)
            //{success->

           // }
        }

    }
    fun addPendingLikeUpdate(postId: String,like: Boolean){
        pendingLikeUpdates.put(postId,like)
    }

    fun completePendingUpdates(userId:String){
        Log.d("pending xxo","pending updates completion triggered ${pendingLikeUpdates.size}")
        GlobalScope.launch(Dispatchers.IO) {
            pendingLikeUpdates.forEach {
                addLike(it.key,userId,it.value)

            }
            pendingLikeUpdates.clear()
        }

    }



    fun addComment(context: Context,commentId:String,userId: String,postId: String,imageUri: Uri?,text:String){
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.addComment(commentId,context,userId,postId,imageUri,text)
        }
    }

    fun completePendingComment(postId: String,context: Context,userId: String){
        GlobalScope.launch(Dispatchers.IO) {
            pendingComments.forEach{
                addComment(context,it.commentId,userId,it.postId,it.imageUri,it.text)
            }
            pendingComments.clear()
        }
    }

    fun addPendingComment(context: Context,userId: String,postId: String,imageUri: Uri?,text:String,commentId:String){
        pendingComments.add(Comment(commentId = commentId,postId = postId, imageUri = imageUri, text = text))
    }
//
//    fun replyToComment(context: Context,postId: String,userId:String,text:String,imageUri:Uri?,commentId: String){
//        viewModelScope.launch(Dispatchers.IO) {
//            postRepository.replyToComment(context,postId,userId,text,imageUri,commentId)
//        }
//    }



    fun updateComments(context: Context,userId: String,postId:String,commentId: String,like:Boolean?,replies:List<Comment>?){
        CoroutineScope(Dispatchers.IO).launch {
            postRepository.handleCommentUpdates(context,userId,postId,commentId,like, replies)
        }
    }

    fun completePendingCommentUpdates(postId: String,context: Context,userId: String){
        GlobalScope.launch(Dispatchers.IO) {
            pendingCommentUpdates.forEach{
                updateComments(context,userId,postId,it.key,it.value.like,it.value.replies)
            }
            pendingCommentUpdates.clear()
        }
    }


    fun addPendingCommentUpdates(commentId:String,like: Boolean?,reply:Comment?){
        if(pendingCommentUpdates.containsKey(commentId)){
            if(like!=null){
                pendingCommentUpdates.get(commentId)!!.like = like
            }
            if(reply!=null){
                val commentUpdate = pendingCommentUpdates.get(commentId)
                if(commentUpdate!!.replies!=null) {
                    commentUpdate.replies!!.add(reply)
                }
                else{
                    commentUpdate.replies = mutableListOf()
                    commentUpdate.replies!!.add(reply)
                }
            }
        }
        else{
            val update = CommentUpdates()
            if(like!=null) {
                update.like = like
            }
            if(reply!=null){
                update.replies = mutableListOf()
                update.replies!!.add(reply)
            }
            pendingCommentUpdates.put(commentId,update)
        }
    }



    fun fetchComments(postId:String){
        _commentsList.value= mutableListOf()
        _commentersList.value = mutableMapOf()
        viewModelScope.launch {
            postRepository.fetchComments(postId)
                .catch{e->
                    Log.d("Error postvm xxo","Error in post fetching")
                }
                .onEach {list->
                    list.forEach{comment->
                        Log.d("comments fetching vm xxo","$comment")
                        val commenterData = getCommentersData(comment.userId)
                        _commentersList.value = _commentersList.value.toMutableMap().apply {
                            put(comment.commentId,commenterData)
                        }
                    }
                }
                .collect{
                    Log.d("comments collection xxo","setting flow value ${it.size}")

                        _commentsList.value = it.toList()

//                    val printList = _commentsList.value
//                    printList.forEach{
//                        Log.d("printing comments in flow","$it")
//                    }
                }
        }
    }

    private suspend fun getCommentersData(userId: String): CommenterData {
//        val commenter = CommenterData()
        val commenterData =
            suspendCoroutine{ continuation ->

                    postRepository.fetchCommenterData(userId) {
                        continuation.resume(CommenterData(userId,it!!.userName,it!!.userProfilePicUrl))
                    }
            }
        return commenterData

    }

    fun fetchReplies(postId:String){
        _repliesList.value = mutableMapOf()
        viewModelScope.launch {
            postRepository.fetchReplies(postId)
                .catch { e->
                    Log.d("Error postvm xxo","Error in post fetching")
                }
                .onEach { commentList->
                    commentList.forEach{comment->
                        val commenterData = getCommentersData(comment.userId)
                        _commentersList.value = _commentersList.value.toMutableMap().apply {
                            put(comment.commentId,commenterData)
                        }
                    }
                }
                .collect{
                    val map = it.associateBy { it.commentId }
                    _repliesList.value = map.toMutableMap()
                }
        }
    }

    fun reset() {
        _postUploadProgress.postValue(null)
    }
}