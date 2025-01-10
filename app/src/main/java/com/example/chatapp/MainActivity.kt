package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chatapp.Authentication.SignUpFragment
import com.example.chatapp.Utilities.FirebaseService
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val currentUser = FirebaseService.firebaseAuth.currentUser
        if(currentUser==null)
        loadSignUpFragment()
        else{
            val intent = Intent(this,MainActivity2::class.java)
            startActivity(intent)
        }

    }

    private fun loadSignUpFragment(){
        val signUpFragment = SignUpFragment()
        supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container,signUpFragment).commit()

    }
}