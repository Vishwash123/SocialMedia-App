package com.example.chatapp.Models

import com.google.firebase.Timestamp

data class Message(
    val messageId:String="",
    val senderId:String="",
    val content:String="",
    val timestamp: Long = 0L,
    val messageType: MessageType = MessageType.TEXT,
    val mediaUrl:String? = null,
    val mediaSize:Long? = null,
    val isSeen:Boolean = false

)

enum class MessageType{
    TEXT,IMAGE,VIDEO,AUDIO,DOCUMENT
}
