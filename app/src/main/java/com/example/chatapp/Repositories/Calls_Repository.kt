package com.example.chatapp.Repositories

import android.util.Log
import com.example.chatapp.Models.Call
import com.example.chatapp.Models.CallStatus
import com.example.chatapp.Models.User
import com.example.chatapp.Utilities.FirebaseService
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.tasks.await
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class Calls_Repository @Inject constructor() {
    private val database = FirebaseService.firebaseDatabase

    fun addNewCall(call: Call){
        val callId = call.callId
        saveCallToDatabase(call,callId)
    }

    private fun saveCallToDatabase(call: Call, callId: String) {

        database.reference.child("Calls/$callId").setValue(call).addOnCompleteListener {task->
            if(task.isSuccessful){
                val senderRef = database.reference.child("users/${call.caller}/callIds")
                senderRef.runTransaction(object :Transaction.Handler{
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val currentList = currentData.getValue<List<String>>()?: emptyList()
                        val updatedList = currentList + callId
                        currentData.value = updatedList
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        if(error!=null){
                            return
                        }

                        val receiverRef = database.reference.child("users/${call.receiver}/callIds")
                        receiverRef.runTransaction(object :Transaction.Handler{
                            override fun doTransaction(currentData: MutableData): Transaction.Result {
                                val currentList = currentData.getValue<List<String>>()?: emptyList()
                                val updatedList = currentList + callId
                                currentData.value = updatedList
                                return Transaction.success(currentData)
                            }

                            override fun onComplete(
                                error: DatabaseError?,
                                committed: Boolean,
                                currentData: DataSnapshot?
                            ) {
                                if(error!=null){
                                    return
                                }
                            }

                        })
                    }

                })
            }
        }
    }

    fun fetchCalls(userId:String,onResult:(List<Call>)->Unit){
        val ref = database.reference.child("users/$userId/callIds")
//        ref.addValueEventListener(object :ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if(snapshot.exists()){
//                    val calls = snapshot.children.mapNotNull { it.getValue(String::class.java) }
//                    fetchCallsById(calls,onResult)
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                onResult(emptyList())
//            }
//
//        })
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val callId = snapshot.getValue(String::class.java)
                if (callId != null) {
                    fetchCallDetails(callId, onResult)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val callId = snapshot.getValue(String::class.java)
                if (callId != null) {
                    fetchCallDetails(callId, onResult)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Handle call removal if needed
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle call move if needed
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())  // Handle error
            }
        })
    }

    private fun fetchCallDetails(callId: String, onResult: (List<Call>) -> Unit) {
        database.reference.child("Calls/$callId").get()
            .addOnSuccessListener { snapshot ->
                val call = snapshot.getValue(Call::class.java)
                if (call != null) {
                    onResult(listOf(call))  // Pass the fetched call
                }
            }
            .addOnFailureListener {
                onResult(emptyList())  // Handle failure
            }
    }

//    private fun fetchCallsById(callIds: List<String>, onResult: (List<Call>) -> Unit) {
//        val calls = mutableListOf<Call>()
//        val remainingCalls = AtomicInteger(callIds.size)
//        callIds.forEach{id->
//            database.reference.child("Calls/$id").get()
//                .addOnSuccessListener { snapshot->
//                    snapshot.getValue(Call::class.java)?.let { calls.add(it) }
//
//                }
//                .addOnCompleteListener{
//                    if(remainingCalls.decrementAndGet()==0) onResult(calls)
//                }
//                .addOnFailureListener {
//                    if (remainingCalls.decrementAndGet()==0) onResult(calls)
//                }
//        }
//
//
//    }

//    suspend fun getUserCalls(userId: String): List<Call> {
//        val userCalls = mutableListOf<Call>()
//        try {
//            // Fetch the user's data to get the list of callIds
//            val userSnapshot = database.reference.child("users").child(userId).get().await()
//            val callIds = userSnapshot.child("callIds").children.map { it.value as String }
//
//            // Fetch each call by its ID
//            for (callId in callIds) {
//                val callSnapshot = database.reference.child("calls").child(callId).get().await()
//                val call = callSnapshot.getValue(Call::class.java)
//                call?.let {
//                    userCalls.add(it)
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        return userCalls
//    }
//
//
//    suspend fun getUserDetails(userId: String): User? {
//        return try {
//            // Use Firebase Realtime Database to fetch data asynchronously
//            val snapshot = database.reference.child("users").child(userId).get().await() // Ensure you use 'await()' with Firebase
//            snapshot.getValue(User::class.java)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }

    // Fetch a specific call by its ID
//    fun getCallDetails(callId: String, callback: (Call) -> Unit) {
//        Log.d("calls repo xxo","inside")
//        val callRef = database.reference.child("Calls").child(callId)
//
//        callRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val call = snapshot.getValue(Call::class.java) ?: Call() // Return an empty Call if none exists
//                callback(call)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                callback(Call()) // Handle error case with an empty call
//            }
//        })
//    }

    fun getCallDetails(callId: String, callback: (Call) -> Unit) {
        Log.d("calls repo xxo", "Fetching call with ID: $callId")
        val callRef = database.reference.child("Calls").child(callId)

        callRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val call = snapshot.getValue(Call::class.java) ?: Call()
                Log.d("calls repo xxo", "Fetched Call: $call")
                callback(call)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("calls repo xxo", "Error fetching call: ${error.message}")
                callback(Call()) // Handle error case with an empty call
            }
        })
    }

    fun updateCall(callId:String,duration:Long = 0,status:CallStatus){
        database.reference.child("Calls/$callId").child("duration").setValue(duration)
        database.reference.child("Calls/$callId").child("status").setValue(status.toString())


    }
}