package com.example.chatapp.ViewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.chatapp.Models.Call
import com.example.chatapp.Models.CallStatus
import com.example.chatapp.Models.Calltype
import com.example.chatapp.Models.User
import com.example.chatapp.Repositories.Calls_Repository
import com.example.chatapp.Repositories.User_Repository
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.Utilities.ZegoUtil
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlin.time.Duration


@HiltViewModel
class CallsViewModel @Inject constructor(
    private val repository: Calls_Repository,
    private val userRepository:User_Repository
) :ViewModel(){

    //add calls
    //update calls
    //fetch calls

//    private val _callsList = MutableLiveData<List<Call>>()
//    val callsList: LiveData<List<Call>> = _callsList
private val _callsList = MutableLiveData<List<Call>>()
    val callsList: LiveData<List<Call>> get() = _callsList

    // LiveData to observe in Fragment for the user-to-call mapping
    private val _callUserMap = MutableLiveData<Map<String, Pair<User?, User?>>>()
    val callUserMap: LiveData<Map<String, Pair<User?, User?>>> get() = _callUserMap


    fun addCall(callId:String,callerId:String, receiverId:String, callType:Calltype){
        ZegoUtil.currentActiveCallId=callId
        val call = Call(callId,callerId,receiverId,System.currentTimeMillis(),0,callType)
        repository.addNewCall(call)
    }

//    fun fetchCalls(userId:String){
//        repository.fetchCalls(userId){list->
//            _callsList.postValue(list)
//
//        }
//    }



    fun updateCalls(callId:String,duration:Long,status: CallStatus){
        repository.updateCall(callId,duration,status)
        fetchCallsForUser(FirebaseService.firebaseAuth.currentUser!!.uid)
    }



//    fun fetchCallsForUser(userId: String) {
//        // First, get the call IDs associated with the user
//        userRepository.getUserCalls(userId) { callIds ->
//            // Once we have the call IDs, fetch the corresponding calls and users
//            val callsList = mutableListOf<Call>()
//            val callUserMap = mutableMapOf<String, Pair<User?, User?>>()
//
//            callIds.forEach { callId ->
//                repository.getCallDetails(callId) { call ->
//                    // After fetching each call, get the caller and receiver user details
//                    userRepository.getUserDetails(call.caller) { caller ->
//                        userRepository.getUserDetails(call.receiver) { receiver ->
//                            // Map the call ID to the caller and receiver
//                            callUserMap[callId] = Pair(caller, receiver)
//                            callsList.add(call)
//
//                            // After all calls are fetched, update the LiveData
//                            if (callsList.size == callIds.size) {
//                                _callsList.value = callsList
//                                _callUserMap.value = callUserMap
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    fun fetchCallsForUser(userId: String) {
        // First, get the call IDs associated with the user
        Log.d("CallsViewModel","boom inside")
        userRepository.getUserCalls(userId) { callIds ->
            // Once we have the call IDs, fetch the corresponding calls and users
            val callsList = mutableListOf<Call>()
            val callUserMap = mutableMapOf<String, Pair<User?, User?>>()

            var callsFetched = 0 // To keep track of how many calls are fetched
            Log.d("CallsViewModel","boom before loop")
            callIds.forEach { callId ->
                repository.getCallDetails(callId) { call ->
                    Log.d("CallsViewModel", "Fetched call: $callId")
                    // After fetching each call, get the caller and receiver user details
                    userRepository.getUserDetails(call.caller) { caller ->
                        userRepository.getUserDetails(call.receiver) { receiver ->
                            // Map the call ID to the caller and receiver
                            callUserMap[callId] = Pair(caller, receiver)
                            callsList.add(call)

                            callsFetched++

                            // After all calls are fetched, update the LiveData
                            if (callsFetched == callIds.size) {
                                Log.d("CallsViewModel", "All calls fetched. Updating LiveData.")
                                _callsList.postValue(callsList)  // postValue for background thread
                                _callUserMap.postValue(callUserMap)  // postValue for background thread
                            }
                        }
                    }
                }
            }
        }
    }

    // Fetch a specific call by its ID




}