package com.example.chatapp.ViewModels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.Models.User
import com.example.chatapp.Repositories.User_Repository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val userRepository: User_Repository) : ViewModel(){
    private val _signUpResult = MutableLiveData<Result<FirebaseUser?>>()
    val signUpResult:LiveData<Result<FirebaseUser?>> = _signUpResult

    private val _logInResult = MutableLiveData<Result<FirebaseUser?>>()
    val logInResult:LiveData<Result<FirebaseUser?>> = _logInResult

    private val _fetchUserResult = MutableLiveData<Result<User?>>()
    val fetchUserResult:LiveData<Result<User?>> = _fetchUserResult

    private val _overallProgress = MutableLiveData<Int>()
    val overallProgress: LiveData<Int> = _overallProgress



    private var signUpProgress = 0
    private var imageUploadProgress = 0

    private val _signOutResult = MutableLiveData<Boolean>()
    val signOutResult:LiveData<Boolean> = _signOutResult

//    fun signUp(email:String,password:String,user: User,imageUri: Uri?,context: Context){
//        viewModelScope.launch {
//           userRepository.signUp(email, password, user,imageUri, context){prog->
//                updateProgress(prog)
//            }
//
//        }
//    }

    fun signUp(email: String, password: String, user: User, imageUri: Uri?, context: Context) {
        viewModelScope.launch {
            // Reset progress states
            resetProgress()

            // Call the repository's signUp function
            userRepository.signUp(email, password, user, imageUri, context, onProgress = { progress ->
                updateProgress(progress)
            }) { result ->
                // Handle result completion
                result.onSuccess { firebaseUser ->
                    Log.d("UserViewModel", "SignUp successful with UID: ${firebaseUser?.uid}")
                    _signUpResult.postValue(Result.success(firebaseUser))
                }.onFailure { exception ->
                    Log.e("UserViewModel", "SignUp failed: ${exception.message}")
                    _signUpResult.postValue(Result.failure(exception))
                }
            }
        }
    }

    private fun updateProgress(progress:Int){


        val totalProgress = progress
        _overallProgress.postValue(totalProgress)

        if(totalProgress==100){
            Log.d("viewmodel","success in signing shoudl fire intent")
            _signUpResult.postValue(Result.success(null))
        }
    }

    private fun resetProgress() {
        signUpProgress = 0
        imageUploadProgress = 0
        _overallProgress.postValue(0)
    }

    fun login(email: String,password: String){
        viewModelScope.launch {
            val result = userRepository.logIn(email,password)
            _logInResult.postValue(result)
        }
    }


    fun fetchUser(uid:String){
        viewModelScope.launch {
            val result = userRepository.fetchUser(uid)
            _fetchUserResult.postValue(result)
        }
    }


    fun signOut(){
        viewModelScope.launch {
            userRepository.signOut()
            _signOutResult.postValue(true)
        }
    }



}