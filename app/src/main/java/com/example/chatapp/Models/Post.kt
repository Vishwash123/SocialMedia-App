package com.example.chatapp.Models

data class Post(
    val postId:String="",
    val posterId:String="",
    val posterName:String="",
    val posterProfilePic:String="",
    val timestamp:Long=0L,
    var likes:Int = 0,
    val comments:Int = 0,
    val likers:MutableList<String> = mutableListOf(),
    val commentIds:List<String> = mutableListOf(),
    var mediaUrls:List<String> = mutableListOf(),
    val slides:Int=0,
    val caption:String = "",
    val postType:PostType?=null
)

enum class PostType{
    PHOTO,VIDEO,COMBINED,UNKNOWN
}