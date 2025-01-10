package com.example.chatapp.Models

data class GroupChat(
    val groupId:String="",
    val groupName:String="",
    val members:MutableList<String> = mutableListOf(),
    val lastMessageId:String?=null,
    var lastUpdatedOn:Long = 0L,
    val messageIds:MutableList<String> = mutableListOf()
)
