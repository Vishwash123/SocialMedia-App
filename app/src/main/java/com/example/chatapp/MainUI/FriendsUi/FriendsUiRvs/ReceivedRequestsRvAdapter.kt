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

class ReceivedRequestsRvAdapter(
    val context: Context,
    val receivedRequests:List<FriendRequest>,
    val userDetails:Map<String, User>,
    val onAccept:(FriendRequest)->Unit,
    val onReject:(FriendRequest)->Unit,
    val onItemClicked: (User)->Unit
):RecyclerView.Adapter<ReceivedRequestsRvAdapter.ReceivedRequestsViewHolder>() {
    inner class ReceivedRequestsViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val profilePic = itemView.findViewById<CircleImageView>(R.id.receivedReqProfilePic)
        val name = itemView.findViewById<TextView>(R.id.receivedUserName)
        val acceptButton = itemView.findViewById<TextView>(R.id.receivedAccept)
        val rejectButton = itemView.findViewById<TextView>(R.id.receivedDecline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceivedRequestsViewHolder {
        return ReceivedRequestsViewHolder(
            LayoutInflater.from(context).inflate(R.layout.received_request_rv_item,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return receivedRequests.size
    }

    override fun onBindViewHolder(holder: ReceivedRequestsViewHolder, position: Int) {
        val currentReq = receivedRequests[position]
        val senderUser = userDetails[currentReq.fromUserId]

        holder.name.text = senderUser?.name

        Glide.with(context)
            .load(senderUser?.profilePic)
            .placeholder(R.drawable.ic_person_placeholder)
            .into(holder.profilePic)

        holder.acceptButton.setOnClickListener{
            onAccept(currentReq)
        }

        holder.itemView.setOnClickListener{
            onItemClicked(senderUser!!)
        }

        holder.rejectButton.setOnClickListener{
            onReject(currentReq)
        }


    }
}