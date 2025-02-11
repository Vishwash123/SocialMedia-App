package com.example.chatapp.Utilities

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.MainUI.ProfileUI.Comments
import com.example.chatapp.MainUI.ProfileUI.OtherUserProfile
import com.example.chatapp.R

object Util {

    var friendsViewPageState:Int = 0
    fun loadFragment(fragmentManager: FragmentManager, fragment: Fragment , backStack:Boolean){
        if(backStack) fragmentManager.beginTransaction().replace(R.id.mainuiFragContainer,fragment).addToBackStack(null).commit()
        else fragmentManager.beginTransaction().replace(R.id.mainuiFragContainer,fragment).commit()
    }
    fun getDividerItemDecoration(rvcontext: Context,orientation:Int=RecyclerView.HORIZONTAL):DividerItemDecoration {
        return DividerItemDecoration(rvcontext,orientation)
    }

    fun loadOtherUserProfile(id:String,fragmentManager: FragmentManager , backStack:Boolean){
        val fragment = OtherUserProfile()
        val bundle = Bundle()
        bundle.putString("id",id)
        fragment.arguments = bundle
        if(backStack) fragmentManager.beginTransaction().replace(R.id.mainuiFragContainer,fragment).addToBackStack(null).commit()
        else fragmentManager.beginTransaction().replace(R.id.mainuiFragContainer,fragment).commit()
    }


    fun loadCommentSection(fragmentManager: FragmentManager,backStack: Boolean,postId:String){
        val fragment = Comments()
        val bundle = Bundle()
        bundle.putString("postId",postId)
        fragment.arguments = bundle
        if(backStack) fragmentManager.beginTransaction().replace(R.id.mainuiFragContainer,fragment).addToBackStack(null).commit()
        else fragmentManager.beginTransaction().replace(R.id.mainuiFragContainer,fragment).commit()
    }


    fun formatTime(timestamp: Long): CharSequence? {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }






}