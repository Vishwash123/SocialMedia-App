package com.example.chatapp.MainUI.FeedUI

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chatapp.Utilities.AttachmentUtils
import com.example.chatapp.Utilities.AuthUtils

class NewPostVPAdapter(
    val context: Context,
    val fragmentManager: FragmentManager,
    val lifecycle: Lifecycle,
    val list:List<Uri>
):FragmentStateAdapter(fragmentManager,lifecycle){
    override fun getItemCount(): Int {
        return list.size
    }

    override fun createFragment(position: Int): Fragment {
        val path = AuthUtils.getRealPathFromURI(context,list[position])
        val type = AttachmentUtils.getFileTypeFromUri(list[position],path,context)

        Log.d("new post xxp rv","${list[position]} : $path and type : $type")
        when(type){
            "image"->{
                return ImageFragment.newInstance(list[position])
            }
            else-> return VideoFragment.newInstance(list[position])
        }
    }


}

/*
*
*  private val VIEW_TYPE_IMAGE = 1
    private val VIEW_TYPE_VIDEO = 2
    inner class NewPostImageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val photo:ImageView = itemView.findViewById(R.id.postImageItemPhoto)
        fun bind(uri: Uri){
            Glide.with(context).load(uri).placeholder(R.drawable.image).into(photo)
        }
    }

    inner class NewPostVideoViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val playerView:PlayerView = itemView.findViewById(R.id.postVideoItemPlayer)
        fun bind(uri: Uri){
            val player = ExoPlayer.Builder(context).build()
            playerView.player = player
            val mediaItem = MediaItem.fromUri(uri)
            player.setMediaItem(mediaItem)
            player.prepare()
        }
    }

    override fun getItemViewType(position: Int): Int {
        val path = AuthUtils.getRealPathFromURI(context,list[position])
        val type = AttachmentUtils.getFileTypeFromUri(list[position],path,context)
        when(type){
            "image"->{
                return VIEW_TYPE_IMAGE
            }
            else-> return VIEW_TYPE_VIDEO
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       if(viewType==VIEW_TYPE_IMAGE){
           return NewPostImageViewHolder(
               LayoutInflater.from(context).inflate(R.layout.post_image_item,parent,false)
           )
       }
        else{
            return NewPostVideoViewHolder(
                LayoutInflater.from(context).inflate(R.layout.post_video_item,parent,false)
            )
       }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]
        if(holder is NewPostImageViewHolder){
            holder.bind(currentItem)
        }
        else if(holder is NewPostVideoViewHolder){
            holder.bind(currentItem)
        }
    }

* */