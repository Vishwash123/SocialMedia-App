package com.example.chatapp.Models

data class FriendRequest(
    val requestId:String,
    val fromUserId:String,
    val toUserId:String,
    val status:String
)
    {
        // No-argument constructor for Firebase
        constructor() : this("", "", "", "")
    }

