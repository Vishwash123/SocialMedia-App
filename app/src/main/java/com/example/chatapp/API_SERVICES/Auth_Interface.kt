package com.example.chatapp.API_SERVICES

import com.example.chatapp.Models.User
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface Auth_Interface {
    @PUT("users/{userId}.json")
    suspend fun createUser(
        @Path("userId") userId:String,
        @Body user:User
    ): Response<Void>

    @GET("users/{userId}.json")
    suspend fun getUsers(
        @Path("userId") userId:String,

    ): Response<User>

}