package com.example.chatapp.MainUI.CallsUI

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.Models.User
import com.example.chatapp.R
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import de.hdodenhof.circleimageview.CircleImageView

class NewCallsRvAdapter(
    val context: Context,
    val friends:List<User>,
    val onAudioClicked:(ZegoSendCallInvitationButton,User)->Unit,
    val onVideoClicked:(ZegoSendCallInvitationButton,User)->Unit
    ):RecyclerView.Adapter<NewCallsRvAdapter.NewCallViewHolder>() {
        inner class NewCallViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
            val name:TextView = itemView.findViewById(R.id.newCallName)
            val photo:CircleImageView = itemView.findViewById(R.id.newCallProfilePic)
            val audioButton:ZegoSendCallInvitationButton = itemView.findViewById(R.id.newCallAudioButton)
            val videoButton:ZegoSendCallInvitationButton = itemView.findViewById(R.id.newCallVideoButton)

            fun bind(user:User){
                name.text = user.name
                Glide.with(context)
                    .load(user.profilePic)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .into(photo)

                audioButton.setOnClickListener(View.OnClickListener {
                    onAudioClicked(audioButton,user)
                })

                videoButton.setOnClickListener(View.OnClickListener {
                    onVideoClicked(videoButton,user)
                })
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewCallViewHolder {
        return NewCallViewHolder(
            LayoutInflater.from(context).inflate(R.layout.new_call_rv_item,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    override fun onBindViewHolder(holder: NewCallViewHolder, position: Int) {
        val currentFriend = friends[position]
        holder.bind(currentFriend)
    }


}