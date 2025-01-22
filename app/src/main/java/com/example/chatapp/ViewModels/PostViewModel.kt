package com.example.chatapp.ViewModels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapp.Models.PostType
import com.example.chatapp.Repositories.Post_repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: Post_repository
):ViewModel(){

    private val _postUploadProgress = MutableLiveData<Int?>()
    val postUploadProgress: LiveData<Int?> = _postUploadProgress

    private val _postUploadSuccess = MutableLiveData<Boolean>()
    val postUploadSuccess:LiveData<Boolean> = _postUploadSuccess

    fun createPost(context: Context,userId: String, uris: List<Uri>, caption: String, postType: PostType){


        Log.d("new posts observer xxo vm","set to ${_postUploadProgress.value}")
        postRepository.createPost(context,userId,uris,caption,postType,
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

    fun reset() {
        _postUploadProgress.postValue(null)
    }
}