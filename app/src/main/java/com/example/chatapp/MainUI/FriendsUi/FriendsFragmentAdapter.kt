package com.example.chatapp.MainUI.FriendsUi

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

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
        else if(position==2)
        {
            ReceivedRequests()
        }
        else{
            throw IllegalStateException("Invalid Position : $position")
        }
    }
    override fun getItemId(position: Int): Long {
        // Return a stable ID based on position
        return position.toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        // Ensure the item ID is valid
        return itemId in 0 until itemCount
    }
}