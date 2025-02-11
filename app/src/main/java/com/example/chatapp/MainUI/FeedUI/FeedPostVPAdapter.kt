package com.example.chatapp.MainUI.FeedUI

import android.content.Context
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chatapp.Utilities.AttachmentUtils

class FeedPostVPAdapter(
    val context: Context,
    val fragmentManager: FragmentManager,
    val lifecycle: Lifecycle,
    val list:List<String>
):FragmentStateAdapter(fragmentManager,lifecycle) {
    override fun getItemCount(): Int {
        return list.size
    }

    override fun createFragment(position: Int): Fragment {
        val currentUrl = list[position]
        val fileType = AttachmentUtils.getFileTypeFromUrl(currentUrl)
        if(fileType=="photo"){
            return ImageFeedFragment.newInstance(currentUrl)
        }
        else{
            return VideoFeedFragment.newInstance(currentUrl)
        }
    }
}