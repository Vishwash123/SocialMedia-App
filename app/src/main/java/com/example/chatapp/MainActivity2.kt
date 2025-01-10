package com.example.chatapp

import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.chatapp.MainUI.Calls
import com.example.chatapp.MainUI.ChatRoom
import com.example.chatapp.MainUI.Chats
import com.example.chatapp.MainUI.ChatsUi.NewChat
import com.example.chatapp.MainUI.ChatsUi.Preview
import com.example.chatapp.MainUI.Feed
import com.example.chatapp.MainUI.Friends
import com.example.chatapp.MainUI.Profile
import com.example.chatapp.Utilities.Util.loadFragment
import com.example.chatapp.databinding.ActivityMain2Binding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.StackFrom
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding
    private lateinit var navView:BottomNavigationView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener { item->
            when(item.itemId){
                R.id.chats->{
                    loadFragment(supportFragmentManager,Chats())
                    true
                }
                R.id.feed->{
                    loadFragment(supportFragmentManager,Feed())
                    true
                }
                R.id.friend->{
                    loadFragment(supportFragmentManager,Friends())
                    true
                }
                R.id.calls->{
                    loadFragment(supportFragmentManager,Calls())
                    true
                }
                R.id.profile->{
                    loadFragment(supportFragmentManager,Profile())
                    true
                }
                else->false

            }
        }

        supportFragmentManager.addOnBackStackChangedListener{
            val currentFragment = supportFragmentManager.findFragmentById(R.id.mainuiFragContainer)
            when(currentFragment){
                is NewChat,is ChatRoom,is Preview->{
                    binding.bottomNav.visibility = View.GONE
                }
                else->{
                    binding.bottomNav.visibility = View.VISIBLE
                }
            }
        }


        if(savedInstanceState==null){
            loadFragment(supportFragmentManager,Chats())
        }

        

    }




    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.mainuiFragContainer)
        if (currentFragment != null && currentFragment.childFragmentManager.backStackEntryCount > 0) {
            currentFragment.childFragmentManager.popBackStack() // Navigate within the current fragment
        } else if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack() // Navigate back to the previous main fragment
        } else {
            super.onBackPressed() // Exit the app or activity
        }
    }




}