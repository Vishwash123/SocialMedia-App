package com.example.chatapp.ViewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapp.Models.FriendRequest
import com.example.chatapp.Models.User
import com.example.chatapp.Repositories.Friend_Repository
import com.example.chatapp.Utilities.FirebaseService
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestoreSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class FriendViewModel @Inject constructor(private val friendRepository: Friend_Repository) :ViewModel(){
    private val _sentRequests = MutableLiveData<List<FriendRequest>>()
    val sentRequests:LiveData<List<FriendRequest>> get() = _sentRequests

    private val _receivedRequests = MutableLiveData<List<FriendRequest>>()
    val receivedRequests:LiveData<List<FriendRequest>>get() = _receivedRequests

    private val _requestResults  = MutableLiveData<Boolean>()
    val requestResults:LiveData<Boolean> get() = _requestResults



    private val _friendList = MutableLiveData<List<User>>()
    val friendList:LiveData<List<User>> = _friendList

    private val _searchResults = MutableLiveData<List<User>>()
    val searchResults:LiveData<List<User>> = _searchResults

    private val _userDetails = MutableLiveData<Map<String,User>>()
    val userDetails:LiveData<Map<String,User>> = _userDetails

    private var areFriendsLoaded = false
    private var areSentRequestsLoaded = false
    private var areReceivedRequestsLoaded = false


    fun sendFriendRequest(fromUserId:String,toUserId:String){
        friendRepository.sendFriendRequest(fromUserId,toUserId){success->
            _requestResults.postValue(success)
        }
    }

    fun cancelFriendRequest(fromUserId: String, requestId: String) {
        friendRepository.cancelFriendRequest(fromUserId, requestId) { success ->
            _requestResults.postValue(success)
        }
    }


    fun respondToFriendRequests(userId: String,requestId:String,accepted:Boolean){
        friendRepository.respondToFriendRequest(requestId,accepted){success->
            _requestResults.postValue(success)
            loadReceivedRequests(userId)
        }
    }

    fun loadSentRequests(userId:String){
        if(areSentRequestsLoaded && _sentRequests.value!=null) {
            _sentRequests.value = _sentRequests.value
            return
        }
        friendRepository.getSentFriendRequests(userId){requests->
            _sentRequests.postValue(requests)
            areSentRequestsLoaded=true

        }
    }

    fun loadReceivedRequests(userId: String){
        if(areReceivedRequestsLoaded && _receivedRequests.value!=null) {
            _receivedRequests.value = _receivedRequests.value
            return
        }
        friendRepository.getReceivedFriendRequests(userId){requests->
            _receivedRequests.postValue(requests)
            areReceivedRequestsLoaded=true
        }
    }

    fun loadFriendsList(userId: String){
        if(areFriendsLoaded && _friendList.value!=null) {
            _friendList.value = _friendList.value
            return
        }
        Log.d("load friends xxxo","loading")
        friendRepository.getFriendList(userId){friends->
            _friendList.postValue(friends)
            areFriendsLoaded=true


        }
    }

    fun searchUsers(userId: String,query: String){
        friendRepository.searchUsers(userId,query){users->
            _searchResults.postValue(users)
        }
    }

    fun removeFriend(userId: String,friendId:String){
        friendRepository.removeFriend(userId,friendId){success->
            if(success){
                loadFriendsList(userId)
            }
            _requestResults.postValue(success)
        }
    }

    fun loadUserDetails(userIds:List<String>){
        friendRepository.getUsersByIds(userIds){users->
            _userDetails.postValue(users.associateBy { it.id })
        }
    }
}