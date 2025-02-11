package com.example.chatapp.MainUI.ProfileUI

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.Models.Comment
import com.example.chatapp.Models.CommenterData
import com.example.chatapp.R
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.Utilities.Util
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text

class RepliesAdapter(
    val context: Context,
    val commentId:String,
    val replies:MutableList<Comment> = mutableListOf(),
    val repliersData:MutableMap<String,CommenterData> = mutableMapOf(),
    val onReplyClicked:(String)->Unit,
    val onLikeClicked:(String,Boolean)->Unit
): RecyclerView.Adapter<RepliesAdapter.RepliesViewHolder>() {
    private var lastClickedTime = 0L
    private val debounceTime = 300L
    private val currentUserId = FirebaseService.firebaseAuth.currentUser!!.uid

    inner class RepliesViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val replierName:TextView=itemView.findViewById(R.id.replyItemUserName)
        val repliersProfilePic:CircleImageView = itemView.findViewById(R.id.replyItemProfilePic)
        val replyTime:TextView = itemView.findViewById(R.id.replyItemTime)
        val replyLikeButton:ImageView = itemView.findViewById(R.id.replyLikeButton)
        val replyLikeCount:TextView = itemView.findViewById(R.id.replyLikeCount)
        val replyReplyButton:TextView = itemView.findViewById(R.id.replyReplyButton)
        val replyImage:ImageView = itemView.findViewById(R.id.replyImage)
        val replyText:TextView = itemView.findViewById(R.id.replyCaption)
        fun bind(comment: Comment){
            replierName.text = repliersData.get(comment.commentId)!!.userName
            Glide.with(context).load(repliersData.get(comment.commentId)!!.userProfilePicUrl).placeholder(R.drawable.ic_person_placeholder).into(repliersProfilePic)
            replyTime.text = Util.formatTime(comment.timestamp)
            replyLikeCount.text = comment.likes.size.toString()
            if(comment.imageUrl==null){
                replyImage.visibility = View.GONE
            }
            else{
                replyImage.visibility = View.VISIBLE
                Glide.with(context).load(comment.imageUrl).into(replyImage)
            }
            replyText.text = comment.text
            val isLiked = comment.likes.contains(currentUserId)

            replyLikeButton.setOnClickListener{
                val currentTime = System.currentTimeMillis()
                if(currentTime - lastClickedTime < debounceTime){
                    return@setOnClickListener
                }
                if(isLiked){
                    lastClickedTime = System.currentTimeMillis()
                    comment.likes.remove(currentUserId)
                    onLikeClicked(comment.commentId,false)
                    notifyDataSetChanged()
                }
                else{
                    lastClickedTime = System.currentTimeMillis()
                    comment.likes.add(currentUserId)
                    onLikeClicked(comment.commentId,true)
                    notifyDataSetChanged()
                }
            }

            replyReplyButton.setOnClickListener{
                onReplyClicked(commentId)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepliesViewHolder {
        return RepliesViewHolder(
            LayoutInflater.from(context).inflate(R.layout.reply_item,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return replies.size
    }

    override fun onBindViewHolder(holder: RepliesViewHolder, position: Int) {
        val reply = replies[position]
        holder.bind(reply)
    }


    fun updateData(updatedReplies: List<Comment>, updatedRepliersData: Map<String, CommenterData>){
        replies.clear()
        replies.addAll(updatedReplies)
        repliersData.clear()
        repliersData.putAll(updatedRepliersData)
        notifyDataSetChanged()
    }

}