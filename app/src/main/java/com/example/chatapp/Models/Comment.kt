package com.example.chatapp.Models

import android.net.Uri
import com.example.chatapp.MainUI.ProfileUI.Comments

data class Comment(
    val commentId:String="",
    val userId:String="",
    val postId:String="",
    val timestamp:Long = 0L,
    val likes:MutableList<String> = mutableListOf(),
    val text:String = "",
    var imageUrl:String? = null,
    val replies:MutableList<String> = mutableListOf(),
    var imageUri: Uri? = null
)


data class CommenterData(
    var userId: String="",
    var userName:String="",
    var userProfilePicUrl:String=""
)