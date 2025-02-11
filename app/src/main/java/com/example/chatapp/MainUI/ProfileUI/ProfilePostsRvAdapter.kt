package com.example.chatapp.MainUI.ProfileUI

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.Models.Post
import com.example.chatapp.R
import com.example.chatapp.Utilities.AttachmentUtils
import com.example.chatapp.Utilities.Util
import com.google.firebase.firestore.bundle.BundleLoader

class ProfilePostsRvAdapter(
    val context: Context,
    val list: List<Post>,
    val fragmentManager: FragmentManager
):RecyclerView.Adapter<ProfilePostsRvAdapter.ProfilePostsViewHolder>(){
    inner class ProfilePostsViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val image:ImageView = itemView.findViewById(R.id.profilePostsRvItemPhoto)
        fun bind(currentPost:Post){
            val firstFile = currentPost.mediaUrls.get(0)
            val fileType = AttachmentUtils.getFileTypeFromUrl(firstFile)
            if(fileType=="photo") {
                Glide.with(context).load(firstFile).into(image)
            }
            else{
                Glide.with(context).asBitmap().load(firstFile).frame(0).into(image)
            }
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
        holder.itemView.setOnClickListener{
            val bundle = Bundle()
            val fragment = SelfPostList()
            bundle.putInt("position",position)
            fragment.arguments = bundle
            Util.loadFragment(fragmentManager,fragment,true)
        }
    }
    
}