package com.example.chatapp.MainUI.CallsUI

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.Models.Call
import com.example.chatapp.Models.CallStatus
import com.example.chatapp.Models.Calltype
import com.example.chatapp.Models.User
import com.example.chatapp.R
import com.example.chatapp.Utilities.FirebaseService
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import de.hdodenhof.circleimageview.CircleImageView

class CallsRvAdapter(
    val context: Context,
    val list: List<Call>,
    val map:Map<String,Pair<User?,User?>>,
    val onAudioClick:(ZegoSendCallInvitationButton,User)->Unit,
    val onVideoClick:(ZegoSendCallInvitationButton,User)->Unit,
    ):RecyclerView.Adapter<CallsRvAdapter.CallsViewHolder>() {

//
//    private val filteredList: List<Call> = list.filter { call ->
//        call.status != CallStatus.ACTIVE // Example: exclude rejected calls
//    }
    inner class CallsViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val name:TextView = itemView.findViewById(R.id.callsName)
        val photo:CircleImageView = itemView.findViewById(R.id.callsProfilePic)
        val status:TextView = itemView.findViewById(R.id.callStatus)
        val time:TextView = itemView.findViewById(R.id.callsTime)
        val duration:TextView = itemView.findViewById(R.id.callsDuration)
        val callIcon:ImageView = itemView.findViewById(R.id.callIcon)
        val callButton:ZegoSendCallInvitationButton = itemView.findViewById(R.id.callType)

        fun bind(call:Call,user: User){
            Log.d("rv adapter xxo","binding $call to $user")
            name.text = user.name.toString()
            time.text = formatTime(call.timestamp)
            Glide.with(context).load(user.profilePic).placeholder(R.drawable.ic_person_placeholder).into(photo)
            when(call.status){
                CallStatus.REJECTED->{
                    duration.visibility=View.GONE
                    if(FirebaseService.firebaseAuth.currentUser!!.uid == call.caller){
                        status.text = "Sent at "
                        callIcon.setImageResource(R.drawable.sent_missed)

                    }
                    else{
                        status.text = "Rejected at "
                        callIcon.setImageResource(R.drawable.incoming_rejected)

                    }
                    status.setTextColor(Color.RED)
                    time.setTextColor(Color.RED)

                }
                CallStatus.MISSED->{
                    duration.visibility=View.GONE
                    if(FirebaseService.firebaseAuth.currentUser!!.uid == call.caller){
                        status.text = "Sent at "
                        callIcon.setImageResource(R.drawable.sent_missed)
                    }
                    else{
                        status.text = "Missed at "
                        callIcon.setImageResource(R.drawable.incoming_missed)
                    }
                    status.setTextColor(Color.RED)
                    time.setTextColor(Color.RED)
                }

                CallStatus.ACCEPTED->{
                    duration.visibility = View.VISIBLE
                    duration.text = call.duration.toString()
                    if(FirebaseService.firebaseAuth.currentUser!!.uid == call.caller){
                        status.text = "Sent at "
                        callIcon.setImageResource(R.drawable.sent_accepted)
                    }
                    else{
                        status.text = "Accepted at "
                        callIcon.setImageResource(R.drawable.incoming_accepter)
                    }
                    status.setTextColor(Color.GREEN)
                    time.setTextColor(Color.GREEN)


                }
                else->{
                    status.text=""
                }
            }


            if(call.type==Calltype.AUDIO){
                callButton.setBackgroundResource(R.drawable.calls_call_icon)
                callButton.setOnClickListener(View.OnClickListener {
                    onAudioClick(callButton,user)
                })
            }
            else{
                callButton.setBackgroundResource(R.drawable.calls_video_icon)
                callButton.setOnClickListener(View.OnClickListener {
                    onVideoClick(callButton,user)
                })
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallsViewHolder {
        return CallsViewHolder(
            LayoutInflater.from(context).inflate(R.layout.calls_rv_item,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return list.filter { it.status != CallStatus.ACTIVE }.size
    }

    override fun onBindViewHolder(holder: CallsViewHolder, position: Int) {
        val currentCall = list.filter { it.status != CallStatus.ACTIVE }[position]  // Dynamically filtered

        Log.d("rv adapter xxo","$currentCall")
        val user:User = if(map[currentCall.callId]!!.first!!.id ==FirebaseService.firebaseAuth.currentUser!!.uid){
            map[currentCall.callId]!!.second!!
        }else{
            map[currentCall.callId]!!.first!!
        }
//        if(currentCall.status == CallStatus.ACTIVE){
//            holder.itemView.visibility = View.GONE
//        }
//        else{
//            holder.itemView.visibility = View.VISIBLE
//        }


        holder.bind(currentCall, user)

    }

    private fun formatTime(timestamp: Long): CharSequence? {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}