package com.example.chatapp.Models

import com.google.firebase.Timestamp
import java.io.Serializable

data class Message(
    val messageId:String="",
    val senderId:String="",
    val content:String="",
    val timestamp: Long = 0L,
    val messageType: MessageType = MessageType.TEXT,
    val mediaUrl:String? = null,
    val mediaSize:Long? = null,
    val isSeen:Boolean = false,

    //progress
    var progress:Int? = 0,
    var isUploading:Boolean = true

):Serializable

enum class MessageType{
    TEXT,IMAGE,VIDEO,AUDIO,DOCUMENT
}
