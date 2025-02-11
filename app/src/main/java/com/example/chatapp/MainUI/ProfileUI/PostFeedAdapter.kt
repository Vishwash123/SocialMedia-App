package com.example.chatapp.MainUI.ProfileUI

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.colormoon.readmoretextview.ReadMoreTextView
import com.example.chatapp.MainUI.FeedUI.FeedPostVPAdapter
import com.example.chatapp.Models.Post
import com.example.chatapp.R
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.Utilities.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PostFeedAdapter (
    val context: Context,
    val list: List<Post>,
    val fragmentManager: FragmentManager,
    val lifecycle: Lifecycle,
    val onLikePressed:(Post,Boolean)->Unit,
    val onCommentPressed:(String)->Unit,
    val onDoubleTap:(Post)->Unit,
):RecyclerView.Adapter<PostFeedAdapter.PostsFeedViewHolder>() {
    private var lastClickedTime = 0L
    private val debounceTime = 300L
    val currentUserId = FirebaseService.firebaseAuth.currentUser!!.uid

    inner class PostsFeedViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val profilePic:ImageView = itemView.findViewById(R.id.feedPostProfilePic)
        val name:TextView = itemView.findViewById(R.id.feedPostName)
        val likes:TextView = itemView.findViewById(R.id.feedPostLikes)
        val comments:TextView = itemView.findViewById(R.id.feedPostComments)
        val likeButton:ImageView = itemView.findViewById(R.id.feedLikeButton)
        val commentButton:ImageView = itemView.findViewById(R.id.feedPostCommentButton)
        val postVp:ViewPager2 = itemView.findViewById(R.id.feedPostVP)
        val caption:ReadMoreTextView = itemView.findViewById(R.id.feedPostCaption)
        val timestamp:TextView = itemView.findViewById(R.id.feedPostTime)


        //metadata
        var currPost = Post();
        var isLiked = false
        fun bind(post: Post){
            currPost = post
            isLiked = post.likers.contains(currentUserId)
            Glide.with(context).load(post.posterProfilePic).into(profilePic)
            name.text = post.posterName
            caption.setText(getFormattedCaptionText(post.posterName,post.caption))
            caption.setCollapsedText("Read More")
            caption.setExpandedText("Read Less")
            caption.setTrimLines(2)
            timestamp.text = Util.formatTime(post.timestamp)
            likes.text = post.likes.toString()
            comments.text = post.comments.toString()

            if(isLiked){
                likeButton.setImageResource(R.drawable.heart_red)
            }
            else{
                likeButton.setImageResource(R.drawable.heart)
            }

            /*

                //Flow for likes :
                create the detahed emthod
                insid it call the bckend update funcuton only if4 the current valeu of the like is different from the one before


            * */

            likeButton.setOnClickListener {
                val currentTime:Long = System.currentTimeMillis()
                Log.d("Post like check xxo","liked button pressed ${currentTime-lastClickedTime < debounceTime}")
                if(currentTime - lastClickedTime < debounceTime) {
                    return@setOnClickListener
                }
                Log.d("Post like check xxo","hello")
                if(isLiked){
                    lastClickedTime = System.currentTimeMillis()
                    post.likes-=1;
                    post.likers.remove(currentUserId)
                    onLikePressed(post,false)
                    notifyDataSetChanged()
                }
                else{
                    lastClickedTime = System.currentTimeMillis()
                    post.likes+=1
                    post.likers.add(currentUserId)
                    onLikePressed(post,true)
                    notifyDataSetChanged()
                }
            }

            commentButton.setOnClickListener{
                onCommentPressed(post.postId)
            }
            //create vp noew

            postVp.adapter = FeedPostVPAdapter(context,fragmentManager,lifecycle,post.mediaUrls)
            postVp.setCurrentItem(0,false)
        }


    }


    private fun getFormattedCaptionText(posterName: String, caption: String): CharSequence? {
        val formattedCaption = "$posterName $caption"
        val spannableString = SpannableString(formattedCaption)
        val start = 0
        val end = posterName.length
        spannableString.setSpan(StyleSpan(Typeface.BOLD),start,end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsFeedViewHolder {
        return PostsFeedViewHolder(
            LayoutInflater.from(context).inflate(R.layout.feed_post_item,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

//    override fun onViewDetachedFromWindow(holder: PostsFeedViewHolder) {
//       val isLiked=  holder.currPost.likers.contains(currentUserId)
//        if(holder.isLiked!=isLiked) {
//            onLikePressed(holder.currPost,isLiked)
//        }
//
//
//    }


    override fun onBindViewHolder(holder: PostsFeedViewHolder, position: Int) {
        val currentPost = list[position]
        holder.bind(currentPost)

    }
}