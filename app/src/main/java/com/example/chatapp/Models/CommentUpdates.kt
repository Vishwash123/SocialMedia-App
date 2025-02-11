package com.example.chatapp.Models

data class CommentUpdates(
    var like:Boolean?=null,
    var replies:MutableList<Comment>? = null
)


