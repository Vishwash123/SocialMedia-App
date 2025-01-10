package com.example.chatapp.MainUI.FriendsUi.FriendsUiRvs

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

class FriendListRvAdapter(
    val context: Context,
    val friendsList:List<User>,
    val onRemoveClick:(User)->Unit):RecyclerView.Adapter<FriendListRvAdapter.FriendListViewHolder>() {
    inner class FriendListViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val friendProfilePic = itemView.findViewById<CircleImageView>(R.id.friendListPhoto)
        val friendName = itemView.findViewById<TextView>(R.id.friendListName)
        val removeButton = itemView.findViewById<TextView>(R.id.friendListRemove)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendListViewHolder {
        return FriendListViewHolder(
            LayoutInflater.from(context).inflate(R.layout.friend_list_rv_item,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return friendsList.size
    }

    override fun onBindViewHolder(holder: FriendListViewHolder, position: Int) {
        val currentItem = friendsList[position]
        holder.friendName.text = currentItem.name

        Glide.with(context)
            .load(currentItem.profilePic)
            .placeholder(R.drawable.ic_person_placeholder)
            .into(holder.friendProfilePic)

        holder.removeButton.setOnClickListener{
            onRemoveClick(currentItem)
        }


    }
}