package com.example.chatapp.Models

import java.io.Serializable

data class OneToOneChat(
    val chatId:String="",
    val user1Id:String="",
    val user2Id:String="",
    var lastMessageId:String?=null,
    var lastUpdatedOn:Long = 0L,
    val messageIds:MutableList<String> = mutableListOf()
):Serializable
