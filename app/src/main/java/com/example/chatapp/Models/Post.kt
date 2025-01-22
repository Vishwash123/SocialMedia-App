package com.example.chatapp.Models

data class Post(
    val postId:String="",
    val posterId:String="",
    val posterProfilePic:String="",
    val timestamp:Long=0L,
    val likes:Int = 0,
    val comments:Int = 0,
    val commentIds:List<String> = mutableListOf(),
    var mediaUrls:List<String> = mutableListOf(),
    val slides:Int=0,
    val caption:String = "",
    val postType:PostType?=null
)

enum class PostType{
    PHOTO,VIDEO,COMBINED,UNKNOWN
}