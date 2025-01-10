package com.example.chatapp.Repositories

import android.util.Log
import com.example.chatapp.Models.FriendRequest
import com.example.chatapp.Models.User
import com.example.chatapp.Utilities.FirebaseService
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Friend_Repository @Inject constructor() {
    private val database = FirebaseService.firebaseDatabase

    fun sendFriendRequest(fromUserId:String,toUserId:String,onComplete:(Boolean)->Unit){

        val requestId = database.reference.child("friendRequests").push().key ?: return
        val friendRequest = FriendRequest(requestId, fromUserId, toUserId, "pending")

        // First, create the friend request in the 'friendRequests' node
        database.reference.child("friendRequests/$requestId").setValue(friendRequest)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Now update the sender's and receiver's sent/received requests as lists of IDs
//                    val updates = mutableMapOf<String, Any?>(
//                        "users/$fromUserId/sentFriendRequests" to arrayListOf(requestId),
//                        "users/$toUserId/receivedFriendRequests" to arrayListOf(requestId)
//                    )
//
//                    // Perform the update for sent/received friend requests
//                    database.reference.updateChildren(updates).addOnCompleteListener { task2 ->
//                        onComplete(task2.isSuccessful)
//                    }

                    val senderRef = database.reference.child("users/$fromUserId/sentFriendRequests")
                    val receiverRef = database.reference.child("users/$toUserId/receivedFriendRequests")

                    senderRef.runTransaction(object : Transaction.Handler {
                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                            val currentList = currentData.getValue<List<String>>() ?: emptyList()
                            val updatedList = currentList + requestId
                            currentData.value = updatedList
                            return Transaction.success(currentData)
                        }

                        override fun onComplete(
                            error: DatabaseError?,
                            committed: Boolean,
                            currentData: DataSnapshot?
                        ) {
                            if (error != null) {
                                onComplete(false)
                                return
                            }

                            // Update the receiver's list in a similar way
                            receiverRef.runTransaction(object : Transaction.Handler {
                                override fun doTransaction(currentData: MutableData): Transaction.Result {
                                    val currentList = currentData.getValue<List<String>>() ?: emptyList()
                                    val updatedList = currentList + requestId
                                    currentData.value = updatedList
                                    return Transaction.success(currentData)
                                }

                                override fun onComplete(
                                    error: DatabaseError?,
                                    committed: Boolean,
                                    currentData: DataSnapshot?
                                ) {
                                    onComplete(error == null && committed)
                                }
                            })
                        }
                    })
                } else {
                    onComplete(false)
                }
            }


    }

    fun fetchFriendsFromMap(snapshot: DataSnapshot): List<User> {
        val friendsList = mutableListOf<User>()
        for (child in snapshot.children) {
            child.getValue(User::class.java)?.let { friendsList.add(it) }
        }
        return friendsList
    }



    fun respondToFriendRequest(requestId: String, accepted: Boolean, onComplete: (Boolean) -> Unit) {
        val requestRef = database.reference.child("friendRequests/$requestId")

        requestRef.get().addOnSuccessListener { snapshot ->
            val request = snapshot.getValue(FriendRequest::class.java)
            if (request != null) {
                val fromUserId = request.fromUserId
                val toUserId = request.toUserId

                // Determine if request is accepted or rejected
                val status = if (accepted) "accepted" else "rejected"

                // Remove request from sent and received lists
                val sentRequestsRef = database.reference.child("users/$fromUserId/sentFriendRequests")
                val receivedRequestsRef = database.reference.child("users/$toUserId/receivedFriendRequests")

                // Fetch current sent requests for the "from" user
                sentRequestsRef.get().addOnSuccessListener { sentSnapshot ->
                    val sentRequestsMap = sentSnapshot.children.associateBy({ it.key }, { it.value as? String }).toMutableMap()

                    // Remove the specific requestId by its key
                    sentRequestsMap.entries.removeIf { it.value == requestId }

                    // Fetch current received requests for the "to" user
                    receivedRequestsRef.get().addOnSuccessListener { receivedSnapshot ->
                        val receivedRequestsMap = receivedSnapshot.children.associateBy({ it.key }, { it.value as? String }).toMutableMap()

                        // Remove the specific requestId by its key
                        receivedRequestsMap.entries.removeIf { it.value == requestId }

                        // Prepare updates for the friend request
                        val updates = mutableMapOf<String, Any?>(
                            "friendRequests/$requestId/status" to status,  // Update request status (accepted/rejected)
                            "users/$fromUserId/sentFriendRequests" to sentRequestsMap,  // Updated sent requests list
                            "users/$toUserId/receivedFriendRequests" to receivedRequestsMap  // Updated received requests list

                        )

                        if (accepted) {
                            // Add users to each other's friends list
                            updates["users/$fromUserId/friends/$toUserId"] = toUserId
                            updates["users/$toUserId/friends/$fromUserId"] = fromUserId

                        }

                        database.reference.child("friendRequests").child(requestId).removeValue()

                        // Apply the updates to the database
                        database.reference.updateChildren(updates).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onComplete(true)  // Successfully updated
                            } else {
                                onComplete(false)  // Failed to apply updates
                            }
                        }.addOnFailureListener {
                            onComplete(false)  // Handle update failure
                        }
                    }.addOnFailureListener {
                        onComplete(false)  // Failed to fetch received requests
                    }
                }.addOnFailureListener {
                    onComplete(false)  // Failed to fetch sent requests
                }
            } else {
                // If the request doesn't exist, handle failure
                onComplete(false)
            }
        }.addOnFailureListener {
            // Handle failure to retrieve the request data (e.g., network issues)
            onComplete(false)
        }
    }



//
//    fun respondToFriendRequest(requestId:String,accepted:Boolean,onComplete: (Boolean) -> Unit){
//        val requestRef = database.reference.child("friendRequests/$requestId")
//
//        requestRef.get().addOnSuccessListener { snapshot ->
//            val request = snapshot.getValue(FriendRequest::class.java)
//            if (request != null) {
//                val updates = mutableMapOf<String, Any?>()
//
//                // Update the request's status
//                updates["friendRequests/$requestId/status"] = if (accepted) "accepted" else "rejected"
//
//                if (accepted) {
//                    // Add users to each other's friends list
//                    updates["users/${request.fromUserId}/friends/${request.toUserId}"] = request.toUserId
//                    updates["users/${request.toUserId}/friends/${request.fromUserId}"] = request.fromUserId
//                }
//
//                // Remove the friend request from sender's sentFriendRequests
//                val senderRef = database.reference.child("users/${request.fromUserId}/sentFriendRequests/$requestId")
//                val receiverRef = database.reference.child("users/${request.toUserId}/receivedFriendRequests/$requestId")
//
//                senderRef.runTransaction(object : Transaction.Handler {
//                    override fun doTransaction(currentData: MutableData): Transaction.Result {
//                        currentData.value = null
//                        return Transaction.success(currentData)
//                    }
//
//                    override fun onComplete(
//                        error: DatabaseError?,
//                        committed: Boolean,
//                        currentData: DataSnapshot?
//                    ) {
//                        if (error != null) {
//                            onComplete(false)
//                            return
//                        }
//
//                        // Remove the friend request from receiver's receivedFriendRequests
//                        receiverRef.runTransaction(object : Transaction.Handler {
//                            override fun doTransaction(currentData: MutableData): Transaction.Result {
//                                currentData.value = null
//                                return Transaction.success(currentData)
//                            }
//
//                            override fun onComplete(
//                                error: DatabaseError?,
//                                committed: Boolean,
//                                currentData: DataSnapshot?
//                            ) {
//                                if (error == null && committed) {
//                                    // Perform the remaining updates
//                                    database.reference.updateChildren(updates).addOnCompleteListener { task ->
//                                        onComplete(task.isSuccessful)
//                                    }
//                                } else {
//                                    onComplete(false)
//                                }
//                            }
//                        })
//                    }
//                })
//            } else {
//                onComplete(false)
//            }
//        }.addOnFailureListener {
//            onComplete(false)
//        }
//    }




//    fun cancelFriendRequest(fromUserId: String, requestId: String, onComplete: (Boolean) -> Unit) {
//        val requestRef = database.reference.child("friendRequests/$requestId")
//
//        requestRef.get().addOnSuccessListener { snapshot ->
//            val request = snapshot.getValue(FriendRequest::class.java)
//            if (request != null) {
//                val toUserId = request.toUserId
//
//                // Fetch current sent and received friend request lists
//                val sentRequestsRef = database.reference.child("users/$fromUserId/sentFriendRequests")
//                val receivedRequestsRef = database.reference.child("users/$toUserId/receivedFriendRequests")
//
//                sentRequestsRef.get().addOnSuccessListener { sentSnapshot ->
//                    val sentRequests = sentSnapshot.children.mapNotNull { it.key }.toMutableList()
//                    sentRequests.remove(requestId)  // Remove the specific request ID
//
//                    receivedRequestsRef.get().addOnSuccessListener { receivedSnapshot ->
//                        val receivedRequests = receivedSnapshot.children.mapNotNull { it.key }.toMutableList()
//                        receivedRequests.remove(requestId)  // Remove the specific request ID
//
//                        // Perform the database update to reflect the changes
//                        val updates = mutableMapOf<String, Any?>(
//                            "friendRequests/$requestId" to null,  // Remove the friend request
//                            "users/$fromUserId/sentFriendRequests" to sentRequests,  // Updated sent requests list
//                            "users/$toUserId/receivedFriendRequests" to receivedRequests  // Updated received requests list
//                        )
//
//                        // Apply the updates to the database
//                        database.reference.updateChildren(updates).addOnCompleteListener { task ->
//                            onComplete(task.isSuccessful)  // Return success or failure
//                        }
//                    }.addOnFailureListener {
//                        onComplete(false)
//                    }
//                }.addOnFailureListener {
//                    onComplete(false)
//                }
//            } else {
//                // If the request doesn't exist, handle failure
//                onComplete(false)
//            }
//        }.addOnFailureListener {
//            // Handle failure to retrieve the request data (e.g., network issues)
//            onComplete(false)
//        }
//    }


    fun cancelFriendRequest(fromUserId: String, requestId: String, onComplete: (Boolean) -> Unit) {
        val requestRef = database.reference.child("friendRequests/$requestId")

        requestRef.get().addOnSuccessListener { snapshot ->
            val request = snapshot.getValue(FriendRequest::class.java)
            if (request != null) {
                val toUserId = request.toUserId

                // Fetch current sent and received friend request lists
                val sentRequestsRef = database.reference.child("users/$fromUserId/sentFriendRequests")
                val receivedRequestsRef = database.reference.child("users/$toUserId/receivedFriendRequests")

                // Fetch current sent requests for the "from" user
                sentRequestsRef.get().addOnSuccessListener { sentSnapshot ->
                    val sentRequestsMap = sentSnapshot.children.associateBy({ it.key }, { it.value as? String }).toMutableMap()

                    // Remove the specific requestId by its key
                    sentRequestsMap.entries.removeIf { it.value == requestId }

                    // Fetch current received requests for the "to" user
                    receivedRequestsRef.get().addOnSuccessListener { receivedSnapshot ->
                        val receivedRequestsMap = receivedSnapshot.children.associateBy({ it.key }, { it.value as? String }).toMutableMap()

                        // Remove the specific requestId by its key
                        receivedRequestsMap.entries.removeIf { it.value == requestId }

                        // Perform the database update to reflect the changes
                        val updates = mutableMapOf<String, Any?>(
                            "friendRequests/$requestId" to null,  // Remove the friend request
                            "users/$fromUserId/sentFriendRequests" to sentRequestsMap,  // Updated sent requests list
                            "users/$toUserId/receivedFriendRequests" to receivedRequestsMap  // Updated received requests list
                        )

                        // Apply the updates to the database
                        database.reference.updateChildren(updates).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onComplete(true)  // Successfully updated
                            } else {
                                onComplete(false)  // Failed to apply updates
                            }
                        }.addOnFailureListener {
                            onComplete(false)  // Handle update failure
                        }
                    }.addOnFailureListener {
                        onComplete(false)  // Failed to fetch received requests
                    }
                }.addOnFailureListener {
                    onComplete(false)  // Failed to fetch sent requests
                }
            } else {
                // If the request doesn't exist, handle failure
                onComplete(false)
            }
        }.addOnFailureListener {
            // Handle failure to retrieve the request data (e.g., network issues)
            onComplete(false)
        }
    }





    fun getSentFriendRequests(userId:String,onResult:(List<FriendRequest>)->Unit){

        val sentRequestsRef = database.reference.child("users/$userId/sentFriendRequests")

        sentRequestsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // If `sentFriendRequests` exists, get the request IDs
                    val requestIds = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                    fetchFriendRequestsById(requestIds, onResult)
                } else {
                    // If it doesn’t exist, return an empty list
                    onResult(emptyList())
                }
            }


            override fun onCancelled(error: DatabaseError) {
                // Handle any errors by returning an empty list
                onResult(emptyList())
            }
        })

    }

    fun getReceivedFriendRequests(userId: String,onResult: (List<FriendRequest>) -> Unit){

        val receivedRequestsRef = database.reference.child("users/$userId/receivedFriendRequests")

        receivedRequestsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // If `receivedFriendRequests` exists, get the request IDs
                    val requestIds = snapshot.children.mapNotNull {  it.getValue(String::class.java) }
                    fetchFriendRequestsById(requestIds, onResult)
                } else {
                    // If it doesn’t exist, return an empty list
                    onResult(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors by returning an empty list
                onResult(emptyList())
            }
        })

    }

    private fun fetchFriendRequestsById(requestIds: List<String>, onResult: (List<FriendRequest>) -> Unit) {
//
        val requests = mutableListOf<FriendRequest>()
        val remaining = AtomicInteger(requestIds.size)

        requestIds.forEach { id ->
            database.reference.child("friendRequests/$id").get()
                .addOnSuccessListener { snapshot ->
                    snapshot.getValue(FriendRequest::class.java)?.let { requests.add(it) }
                }
                .addOnCompleteListener {
                    if (remaining.decrementAndGet() == 0) onResult(requests)
                }
                .addOnFailureListener {
                    // Log or handle failure here if needed
                    if (remaining.decrementAndGet() == 0) onResult(requests)
                }
        }
    }

    fun getFriendList(userId:String,onResult: (List<User>) -> Unit) {
        val friendsRef = database.reference.child("users/$userId/friends")

        friendsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // If `friends` exists, get the friend IDs
                    val friends = snapshot.children.mapNotNull { it.getValue(String::class.java)  }
                    fetchFriendsById(friends, onResult)
                } else {
                    // If `friends` doesn’t exist, return an empty list
                    onResult(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle cancellation by returning an empty list
                onResult(emptyList())
            }
        })

    }

    private fun fetchFriendsById(friendIds: List<String>, onResult: (List<User>) -> Unit) {
//
        val friends = mutableListOf<User>()
        val remaining = AtomicInteger(friendIds.size)

        friendIds.forEach { id ->
            database.reference.child("users/$id").get()
                .addOnSuccessListener { snapshot ->
                    snapshot.getValue(User::class.java)?.let { friends.add(it) }
                }
                .addOnCompleteListener {
                    if (remaining.decrementAndGet() == 0) onResult(friends)
                }
                .addOnFailureListener {
                    // Log or handle failure here if needed
                    if (remaining.decrementAndGet() == 0) onResult(friends)
                }
        }
    }

    fun searchUsers(userId:String,query:String,onResult: (List<User>) -> Unit){
//        database.reference.child("users").get()
//            .addOnSuccessListener { snapshot->
//                val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
//                    .filter { it.name.contains(query,ignoreCase = true) || it.email.contains(query,ignoreCase = true) }
//                onResult(users)
//            }
        database.reference.child("users").get()
            .addOnSuccessListener { snapshot ->
                val users = mutableListOf<User>()

                // Check if snapshot exists and is not empty
                if (snapshot.exists()) {
                    snapshot.children.forEach { childSnapshot ->
                        // Convert the snapshot to a Map
                        val userid = childSnapshot.key
                        val userMap = childSnapshot.value as? Map<String, Any>

                        userMap?.let { map ->
                            // Convert the map to a User object
                            val user = User(
                                id = map["id"] as? String ?: "",
                                name = map["name"] as? String ?: "",
                                email = map["email"] as? String ?: "",
                                // Add other fields here as needed
                            )

                            // Perform a case-insensitive search by name or email
                            if (userid!=userId && (user.name.contains(query, ignoreCase = true) || user.email.contains(query, ignoreCase = true))) {
                                users.add(user)
                            }
                        }
                    }
                    onResult(users) // Return the list of users
                } else {
                    onResult(emptyList()) // Return an empty list if no users were found
                }
            }
            .addOnFailureListener { exception ->
                Log.e("SearchUsers", "Failed to fetch users", exception)
                onResult(emptyList()) // Return an empty list on failure
            }
    }

    //might add removing listeners function
    fun removeFriend(userId:String,friendId:String,onComplete: (Boolean) -> Unit){
        val updates = mapOf<String,Any?>(
            "users/$userId/friends/$friendId" to null,
            "users/$friendId/friends/$userId" to null
        )
        database.reference.updateChildren(updates).addOnCompleteListener { task->
            onComplete(task.isSuccessful)
        }
    }

    fun getUsersByIds(userIds:List<String>,onResult:(List<User>)->Unit){
//        val users = mutableListOf<User>()
//        userIds.forEach{id->
//            database.reference.child("users/$id").get()
//                .addOnSuccessListener {snapshot->
//                    snapshot.getValue(User::class.java)?.let { users.add(it)  }
//                    if(users.size == userIds.size) onResult(users)
//                }
//        }

        val users = mutableListOf<User>()
        val remaining = AtomicInteger(userIds.size)

        userIds.forEach { id ->
            database.reference.child("users/$id").get()
                .addOnSuccessListener { snapshot ->
                    snapshot.getValue(User::class.java)?.let { users.add(it) }
                }
                .addOnCompleteListener {
                    if (remaining.decrementAndGet() == 0) onResult(users)
                }
                .addOnFailureListener {
                    // Log or handle failure here if needed
                    if (remaining.decrementAndGet() == 0) onResult(users)
                }
        }
    }

}