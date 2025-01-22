package com.example.chatapp.MainUI.ProfileUI

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R

class ProfilePostsRvAdapter(
    val context: Context,
    val list: List<Int>
):RecyclerView.Adapter<ProfilePostsRvAdapter.ProfilePostsViewHolder>(){
    inner class ProfilePostsViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val image:ImageView = itemView.findViewById(R.id.profilePostsRvItemPhoto)
        fun bind(currentPost:Int){
            image.setImageResource(currentPost)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostsViewHolder {
        return ProfilePostsViewHolder(
            LayoutInflater.from(context).inflate(R.layout.profile_posts_rv_item,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ProfilePostsViewHolder, position: Int) {
        val currentPost = list[position]
        holder.bind(currentPost)
    }
}