package com.example.chatapp.MainUI.ChatsUi

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.Models.User
import com.example.chatapp.R
import de.hdodenhof.circleimageview.CircleImageView


class NewChatRvAdapter(
    val context: Context,
    val friends:List<User>,
    val onItemClicked:(String)->Unit
    ):RecyclerView.Adapter<NewChatRvAdapter.NewChatRvViewHolder>() {
    inner class NewChatRvViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val profilePhoto:CircleImageView = itemView.findViewById(R.id.newChatProfilePic)
        val name:TextView = itemView.findViewById(R.id.newChatName)
        val bio:TextView = itemView.findViewById(R.id.newChatBio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewChatRvViewHolder {
        return NewChatRvViewHolder(
            LayoutInflater.from(context).inflate(R.layout.new_chat_rv_item,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    override fun onBindViewHolder(holder: NewChatRvViewHolder, position: Int) {
        val currentFriend = friends[position]
        holder.bio.text = currentFriend.bio
        holder.name.text = currentFriend.name

        Glide.with(context)
            .load(currentFriend.profilePic)
            .placeholder(R.drawable.ic_person_placeholder)
            .into(holder.profilePhoto)

        holder.itemView.setOnClickListener{
            onItemClicked(currentFriend.id)
        }


    }
}