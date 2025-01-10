package com.example.chatapp.MainUI.MainUiRvs

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chatapp.MainUI.FriendsUi.FriendList
import com.example.chatapp.MainUI.FriendsUi.ReceivedRequests
import com.example.chatapp.MainUI.FriendsUi.SentRequests

class FriendsFragmentAdapter(fragmentManger:FragmentManager,lifecycle:Lifecycle):FragmentStateAdapter(fragmentManger,lifecycle) {
    override fun getItemCount(): Int {
        return 3;
    }

    override fun createFragment(position: Int): Fragment {
        return if(position==0)
        {
            FriendList()
        }
        else if(position==1)
        {
            SentRequests()
        }
        else
        {
            ReceivedRequests()
        }
    }
}