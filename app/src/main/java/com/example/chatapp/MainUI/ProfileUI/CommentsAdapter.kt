package com.example.chatapp.MainUI.ProfileUI

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.Models.Comment
import com.example.chatapp.Models.CommenterData
import com.example.chatapp.R
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.Utilities.Util
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.GlobalScope

class CommentsAdapter(
    val context: Context,
    val commentsList:MutableList<Comment>,
    val repliesMap:MutableMap<String,List<Comment>>,
    val commentersData:MutableMap<String,CommenterData>,
    val onLikePressed:(String,Boolean)->Unit,
    val onReplyPressed:(String)->Unit
):RecyclerView.Adapter<CommentsAdapter.commentsViewHolder>() {
    private var lastClickedTime = 0L
    private val debounceTime = 300L
    private val currentUserId = FirebaseService.firebaseAuth.currentUser!!.uid
    inner class commentsViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val commenterName:TextView = itemView.findViewById(R.id.commentItemUserName)
        val commenterPhoto:CircleImageView = itemView.findViewById(R.id.commentItemProfilePic)
        val commentImage:ImageView = itemView.findViewById(R.id.commmentImage)
        val commentText:TextView = itemView.findViewById(R.id.commentCaption)
        val commentLikeButton:ImageView = itemView.findViewById(R.id.commentItemLikeButton)
        val commentLikeCount:TextView = itemView.findViewById(R.id.commentItemLikeCount)
        val replyButton:TextView = itemView.findViewById(R.id.commentItemReplyButton)
        val showHideReplyButton:TextView = itemView.findViewById(R.id.commentItemShowRepliesButton)
        val repliesRv:RecyclerView = itemView.findViewById(R.id.repliesRv)
        val commentTime:TextView = itemView.findViewById(R.id.commentItemTime)

        private var repliesRvAdapter:RepliesAdapter? = null
        fun bind(comment:Comment){
            Log.d("comment rv xxo","${commentsList.size} && ${repliesMap.size} && ${commentersData.size}")
            repliesRv.visibility = View.GONE
            commenterName.text = commentersData.get(comment.commentId)?.userName.toString()
            Glide.with(context).load(commentersData.get(comment.commentId)?.userProfilePicUrl).placeholder(R.drawable.ic_person_placeholder).into(commenterPhoto)
            commentText.text = comment.text
            if(comment.imageUrl==null){
                commentImage.visibility = View.GONE
            }
            else {
                commentImage.visibility = View.VISIBLE
                Glide.with(context).load(comment.imageUrl).into(commentImage)
            }
            commentTime.text = Util.formatTime(comment.timestamp)
            commentLikeCount.text = comment.likes.size.toString()
            val isLiked = comment.likes.contains(currentUserId)
            if(isLiked){
                commentLikeButton.setImageResource(R.drawable.heart_red)
            }
            else{
                commentLikeButton.setImageResource(R.drawable.heart)
            }

            commentLikeButton.setOnClickListener{
                val currentTime = System.currentTimeMillis()
                if(currentTime - lastClickedTime < debounceTime){
                    return@setOnClickListener
                }
                if(isLiked){
                    lastClickedTime = System.currentTimeMillis()
                    comment.likes.remove(currentUserId)
                    onLikePressed(comment.commentId,false)
                    notifyDataSetChanged()
                }
                else{
                    lastClickedTime = System.currentTimeMillis()
                    comment.likes.add(currentUserId)
                    onLikePressed(comment.commentId,true)
                    notifyDataSetChanged()
                }
            }


            replyButton.setOnClickListener{
                onReplyPressed(comment.commentId)
            }

            if(comment.replies.isNotEmpty()){
                showHideReplyButton.visibility = View.VISIBLE
            }
            else{
                showHideReplyButton.visibility = View.GONE
            }

            showHideReplyButton.setOnClickListener{
                if(repliesRv.visibility == View.VISIBLE)
                    repliesRv.visibility = View.GONE
                else {
                    repliesRv.visibility = View.VISIBLE
                    setUpRepliesRv(comment)
                }
            }

        }

        private fun setUpRepliesRv(comment: Comment){
            val replies = mutableListOf<Comment>()
            comment.replies.forEach{
                repliesMap.get(it)?.let { it1 -> replies.add(it1) }
            }
            val repliersData:MutableMap<String,CommenterData> = commentersData.filter { comment.replies.contains(it.key) }.toMutableMap()

            if(repliesRvAdapter==null){
                repliesRvAdapter = RepliesAdapter(
                    context,
                    comment.commentId,
                    replies,
                    repliersData,
                    onReplyPressed,
                    onLikePressed
                )
                repliesRv.layoutManager = LinearLayoutManager(context)
                repliesRv.adapter = repliesRvAdapter
            }
            else{
                repliesRvAdapter!!.updateData(replies,repliersData)
            }
            //repliesRv.layoutManager = LinearLayoutManager(context)


        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): commentsViewHolder {
        return commentsViewHolder(
            LayoutInflater.from(context).inflate(R.layout.comment_item,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return commentsList.size
    }

    override fun onBindViewHolder(holder: commentsViewHolder, position: Int) {
        val currentComment = commentsList[position]
        holder.bind(currentComment)


    }



}