package com.example.chatapp.Models

import java.io.Serializable

data class ChatUIState(
    val chatsList:List<OneToOneChat> = listOf<OneToOneChat>() ,
    val usersMap:Map<String,User> = mapOf<String,User>(),
    val messagesMap:Map<String,Message> = mapOf<String,Message>()
):Serializable
