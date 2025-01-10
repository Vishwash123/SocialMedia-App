package com.example.chatapp.Utilities

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.MainActivity2
import com.example.chatapp.R

object Util {
    fun loadFragment(fragmentManager: FragmentManager, fragment: Fragment){
        fragmentManager.beginTransaction().replace(R.id.mainuiFragContainer,fragment).addToBackStack(null).commit()
    }
    fun getDividerItemDecoration(rvcontext: Context,orientation:Int=RecyclerView.HORIZONTAL):DividerItemDecoration {
        return DividerItemDecoration(rvcontext,orientation)
    }


}