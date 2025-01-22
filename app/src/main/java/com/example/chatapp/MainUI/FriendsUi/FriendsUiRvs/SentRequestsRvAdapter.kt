package com.example.chatapp.MainUI.FriendsUi.FriendsUiRvs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.Models.FriendRequest
import com.example.chatapp.Models.User
import com.example.chatapp.R
import de.hdodenhof.circleimageview.CircleImageView


class SentRequestsRvAdapter(
    val context: Context,
    val sentRequests:List<FriendRequest>,
    val userDetails:Map<String, User>,
    val onCancel:(FriendRequest)->Unit,
    val onItemClicked:(User)->Unit,
    ):RecyclerView.Adapter<SentRequestsRvAdapter.SentRequestsViewHolder>() {
        inner class SentRequestsViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
            val profilePic = itemView.findViewById<CircleImageView>(R.id.sentUserPhoto)
            val name = itemView.findViewById<TextView>(R.id.sentUserName)
            val cancelButton = itemView.findViewById<TextView>(R.id.sentReqCancel)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SentRequestsViewHolder {
        return SentRequestsViewHolder(
            LayoutInflater.from(context).inflate(R.layout.sent_request_rv_item,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return sentRequests.size
    }

    override fun onBindViewHolder(holder: SentRequestsViewHolder, position: Int) {
        val currentReq = sentRequests[position]
        val toUser = userDetails[currentReq.toUserId]

        holder.name.text = toUser?.name

        Glide.with(context)
            .load(toUser?.profilePic)
            .placeholder(R.drawable.ic_person_placeholder)
            .into(holder.profilePic)

        holder.itemView.setOnClickListener{
            onItemClicked(toUser!!)
        }

        holder.cancelButton.setOnClickListener{
            onCancel(currentReq)
        }
    }
}