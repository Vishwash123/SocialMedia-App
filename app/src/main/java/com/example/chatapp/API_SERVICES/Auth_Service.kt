package com.example.chatapp.API_SERVICES

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Auth_Service {

    private val BASE_URL = "https://chat-app-42ddb-default-rtdb.firebaseio.com/"


    val retrofit by lazy{
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
    }

    val service:Auth_Interface by lazy{
        retrofit.create(Auth_Interface::class.java)
    }
}