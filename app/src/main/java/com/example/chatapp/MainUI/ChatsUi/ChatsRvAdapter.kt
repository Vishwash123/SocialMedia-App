package com.example.chatapp.MainUI.ChatsUi

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.Models.Message
import com.example.chatapp.Models.OneToOneChat
import com.example.chatapp.Models.User
import com.example.chatapp.R
import com.example.chatapp.Utilities.FirebaseService
import de.hdodenhof.circleimageview.CircleImageView

class ChatsRvAdapter(
    val context: Context,
    var chats:List<OneToOneChat>,
    var users:Map<String, User>,
    var lastMessages:Map<String,Message>,
    val onItemClicked:(String?)->Unit,
    val onPhotoClicked:(User)->Unit
):RecyclerView.Adapter<ChatsRvAdapter.ChatsViewHolder>() {



    inner class ChatsViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val photo:CircleImageView = itemView.findViewById(R.id.chatProfilePic)
        val name: TextView = itemView.findViewById(R.id.chatName)
        val lastMessage:TextView = itemView.findViewById(R.id.latestMessage)
        val lastMessageTime:TextView = itemView.findViewById(R.id.lastmessagetime)
    }

    private fun formatTime(timestamp: Long): CharSequence? {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        return ChatsViewHolder(
            LayoutInflater.from(context).inflate(R.layout.chats_rv_item,parent,false)
        )
    }


    override fun getItemCount(): Int {
        return chats.size
    }

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        val chat = chats[position]
        val currentUser = FirebaseService.firebaseAuth.currentUser!!
        val otherUser  = if(chat.user1Id==currentUser.uid) chat.user2Id else chat.user1Id
        val user = users[otherUser]
        val message =  lastMessages[chat.lastMessageId]


        holder.name.text = user?.name
        holder.lastMessageTime.text = formatTime(chat.lastUpdatedOn)
        holder.lastMessage.text = message?.content
        Glide.with(context)
            .load(user?.profilePic)
            .placeholder(R.drawable.ic_person_placeholder)
            .into(holder.photo)

        holder.itemView.setOnClickListener{
            onItemClicked(user?.id)
        }
        holder.photo.setOnClickListener{
            onPhotoClicked(user!!)
        }

    }
}