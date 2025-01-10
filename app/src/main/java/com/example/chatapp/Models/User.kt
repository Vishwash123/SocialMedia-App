package com.example.chatapp.Models

import com.google.firebase.database.PropertyName

data class User(
    var id:String="",
    var name:String="",
    val email:String="",
    var profilePic:String="",
    var bio:String = "",
//    var posts:List<String> = emptyList(),
//    var friends:List<String> = emptyList(),
//    var sentFriendRequests:List<String> = emptyList(),
//    var receivedFriendRequests:List<String> = emptyList(),
//    var chats:List<String> = emptyList(),
    @PropertyName("posts") var posts: List<String> = emptyList(),
//    @PropertyName("friends") var friends: List<String> = emptyList(),
//    @PropertyName("sent_friend_requests") var sentFriendRequests: List<String> = emptyList(),
//    @PropertyName("received_friend_requests") var receivedFriendRequests: List<String> = emptyList(),
    @get:PropertyName("friends") @set:PropertyName("friends")
    var friends: Map<String, String> = emptyMap(),

    // Handle `sentFriendRequests` as a map
    @get:PropertyName("sent_friend_requests") @set:PropertyName("sent_friend_requests")
    var sentFriendRequests: Map<String, String> = emptyMap(),

    // Handle `receivedFriendRequests` as a map
    @get:PropertyName("received_friend_requests") @set:PropertyName("received_friend_requests")
    var receivedFriendRequests: Map<String, String> = emptyMap(),

    @PropertyName("chats") var chats: List<String> = emptyList(),

    val createdOn:Long = System.currentTimeMillis(),
    var lastUpdatedOn:Long = System.currentTimeMillis()
){
    // Convert maps to lists dynamically
    val friendIds: List<String>
        get() = friends.values.toList()

    val sentFriendRequestIds: List<String>
        get() = sentFriendRequests.values.toList()

    val receivedFriendRequestIds: List<String>
        get() = receivedFriendRequests.values.toList()


}

