package com.example.chatapp

import android.app.Application
import com.example.chatapp.Utilities.CloudinaryHelper
import com.example.chatapp.Utilities.FirebaseService
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication:Application() {



    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        FirebaseService.firebaseStorage = FirebaseStorage.getInstance()
        FirebaseService.firebaseAuth = FirebaseAuth.getInstance()
        FirebaseService.firebaseAnalytics = Firebase.analytics
        FirebaseService.firebaseDatabase = FirebaseDatabase.getInstance()
        FirebaseService.firebaseStorageReference = FirebaseService.firebaseStorage.reference
        CloudinaryHelper.init(this)
    }

}