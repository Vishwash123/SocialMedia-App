package com.example.chatapp.Models

data class ProfileUiState(
    var name:String="",
    var profilePic:String="",
    var friends:Int=0,
    var posts:Int=0,
    var postIds:List<String> = mutableListOf(),
    var bio:String=""
)
