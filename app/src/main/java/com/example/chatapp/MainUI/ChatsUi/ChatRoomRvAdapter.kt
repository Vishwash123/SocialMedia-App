package com.example.chatapp.MainUI.ChatsUi

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.core.view.marginLeft
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.Models.Message
import com.example.chatapp.Models.MessageType
import com.example.chatapp.R
import com.example.chatapp.Utilities.FirebaseService
import org.w3c.dom.Text

class ChatRoomRvAdapter(
    val context: Context,
    val messages:List<Message>,
    val onVideoClicked:(String)->Unit
    ):RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2
    private val VIEW_TYPE_IMAGE_SENT = 3
    private val VIEW_TYPE_IMAGE_RECEIVED = 4
    private val VIEW_TYPE_VIDEO_SENT = 5
    private val VIEW_TYPE_VIDEO_RECEIVED = 6

    inner class SentMessageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val messageText: TextView = itemView.findViewById(R.id.senderMessageText)
        val messageTime: TextView = itemView.findViewById(R.id.senderMessageSentTime)
        val messageStatus: TextView = itemView.findViewById(R.id.senderMessageStatus)


        fun bind(message:Message){

                messageText.text = message.content
                messageTime.text = formatTime(message.timestamp)
                messageStatus.text = if(message.isSeen) "Sent" else "Seen"


        }
    }


    inner class ReceivedMessageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val messageText:TextView = itemView.findViewById(R.id.receiverMessageText)
        val messageTime:TextView = itemView.findViewById(R.id.receiverMessageSentTime)


        fun bind(message: Message){

                messageText.text = message.content
                messageTime.text = formatTime(message.timestamp)



        }
    }

    inner class ReceivedImageMessageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val image:ImageView = itemView.findViewById(R.id.imageReceivedMessagePhoto)
        val text:TextView = itemView.findViewById(R.id.imageReceivedMessageText)
        val time:TextView = itemView.findViewById(R.id.imageReceivedMessageTime)
        val status:TextView = itemView.findViewById(R.id.imageReceivedMessageStatus)

        fun bind(message: Message){
            text.text = message.content

            time.text = formatTime(message.timestamp)
            Glide.with(context).load(message.mediaUrl).placeholder(R.drawable.ic_person_placeholder).into(image)
        }
    }


    inner class SentImageMessageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val image:ImageView = itemView.findViewById(R.id.imageSentMessagePhoto)
        val text:TextView = itemView.findViewById(R.id.imageSentMessageText)
        val time:TextView = itemView.findViewById(R.id.imageSentMessageTime)
        val status:TextView = itemView.findViewById(R.id.imageSentMessageStatus)
        val imageMessageProgressBar:ProgressBar = itemView.findViewById(R.id.imageMessageProgressBar)

        fun bind(message: Message){
            text.text = message.content
            time.text = formatTime(message.timestamp)
            status.text = if(message.isSeen) "Seen" else "Sent"
            if(message.isUploading){
                imageMessageProgressBar.visibility = View.VISIBLE
                image.setImageResource(R.drawable.image)
                Log.d("chat adpater xxo","${message.progress} in bind")
                imageMessageProgressBar.progress = message.progress ?:0

            }
            else{
                imageMessageProgressBar.visibility = View.GONE
                if(message.mediaUrl!=null){
                    Log.d("chat adpater xxo","loading in bind")
                    Glide.with(context).load(message.mediaUrl).placeholder(R.drawable.ic_person_placeholder).into(image)
                }
                else{
                    Log.d("chat adpater xxo"," not loading in bind")
                    image.setImageResource(R.drawable.ic_person_placeholder)
                }
            }

        }
    }

    inner class ReceivedVideoMessageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val playerView:ImageView = itemView.findViewById(R.id.videoMessageReceivedVideoPlayer)
        val text:TextView = itemView.findViewById(R.id.videoMessageReceivedText)
        val time:TextView = itemView.findViewById(R.id.videoMessageReceivedTime)

        val playButton:ImageView = itemView.findViewById(R.id.videoMessageReceivedPlayButton)

        fun bind(message: Message){
            text.text = message.content
            time.text = formatTime(message.timestamp)
            playButton.visibility = View.VISIBLE
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(message.mediaUrl,HashMap())
            val bitmap = retriever.getFrameAtTime(0)
            playerView.setImageBitmap(bitmap)
            playButton.setOnClickListener{
                onVideoClicked(message.mediaUrl!!)
            }

        }
    }

    inner class SentVideoMessageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val playerView:ImageView = itemView.findViewById(R.id.videoMessageVideoPlayer)
        val text:TextView = itemView.findViewById(R.id.videoMessageText)
        val time:TextView = itemView.findViewById(R.id.videoMessageTime)
        val status:TextView = itemView.findViewById(R.id.videoMessageStatus)
        val progressBar:ProgressBar = itemView.findViewById(R.id.videoMessageProgressBar)
        val playButton:ImageView = itemView.findViewById(R.id.videoMessagePlayButton)

        fun bind(message: Message){
            text.text = message.content
            time.text = formatTime(message.timestamp)
            status.text = if(message.isSeen) "Seen" else "Sent"
            if(message.isUploading){
                progressBar.visibility = View.VISIBLE
                playButton.visibility = View.GONE
                playerView.setImageResource(R.drawable.video)
                progressBar.progress = message.progress?:0

            }
            else{
                progressBar.visibility = View.GONE

                if(message.mediaUrl!=null){
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(message.mediaUrl,HashMap())
                    val bitmap = retriever.getFrameAtTime(0)
                    playerView.setImageBitmap(bitmap)
                    playButton.visibility = View.VISIBLE
                    playButton.setOnClickListener{
                        onVideoClicked(message.mediaUrl)
                    }
                }
                else{
                    playerView.setImageResource(R.drawable.ic_person_placeholder)
                }
            }
        }
    }




    private fun formatTime(timestamp: Long): CharSequence? {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }



    override fun getItemViewType(position: Int): Int {
        if(messages[position].messageType==MessageType.TEXT) {
            return if (FirebaseService.firebaseAuth.currentUser!!.uid == messages[position].senderId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
        }
        else if(messages[position].messageType == MessageType.IMAGE){
            return if (FirebaseService.firebaseAuth.currentUser!!.uid == messages[position].senderId) VIEW_TYPE_IMAGE_SENT else VIEW_TYPE_IMAGE_RECEIVED
        }
        else if(messages[position].messageType == MessageType.VIDEO){
            return if(FirebaseService.firebaseAuth.currentUser!!.uid == messages[position].senderId) VIEW_TYPE_VIDEO_SENT else VIEW_TYPE_VIDEO_RECEIVED
        }
        else return 1;
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType==VIEW_TYPE_SENT){
            return SentMessageViewHolder(
                LayoutInflater.from(context).inflate(R.layout.chat_room_sender_rv_item,parent,false)
            )


        }
        else if(viewType==VIEW_TYPE_IMAGE_SENT){
            return SentImageMessageViewHolder(
                LayoutInflater.from(context).inflate(R.layout.image_message_item,parent,false)
            )
        }
        else if(viewType==VIEW_TYPE_IMAGE_RECEIVED){
            return ReceivedImageMessageViewHolder(
                LayoutInflater.from(context).inflate(R.layout.image_message_received,parent,false)
            )
        }
        else if(viewType==VIEW_TYPE_VIDEO_SENT){
            return SentVideoMessageViewHolder(
                LayoutInflater.from(context).inflate(R.layout.video_message_item,parent,false)
            )
        }
        else if(viewType==VIEW_TYPE_VIDEO_RECEIVED){
            return ReceivedVideoMessageViewHolder(
                LayoutInflater.from(context).inflate(R.layout.video_message_received,parent,false)
            )
        }
        else{
            return ReceivedMessageViewHolder(
                LayoutInflater.from(context).inflate(R.layout.chat_room_receiverrv_item,parent,false)
            )
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if(holder is SentMessageViewHolder){
            holder.bind(message)




        }else if(holder is ReceivedMessageViewHolder){
            holder.bind(message)

        }
        else if(holder is SentImageMessageViewHolder){
            holder.bind(message)
        }
        else if(holder is ReceivedImageMessageViewHolder){
            holder.bind(message)
        }
        else if(holder is SentVideoMessageViewHolder){
            holder.bind(message)
        }
        else if(holder is ReceivedVideoMessageViewHolder){
            holder.bind(message)
        }
    }

    fun updateUploadProgress(progressMap:Map<String,Int>){
        progressMap.forEach{(temporaryId,progress)->
            val position = messages.indexOfFirst { it.messageId==temporaryId }
            if(position!=-1){
                Log.d("chat adpater xxo","${progress}")
                if(progress<100) {
                    messages[position].isUploading = true
                    messages[position].progress = progress
                    notifyItemChanged(position)
                }else{
                    Log.d("chat adpater xxo","${progress} finished")
                    messages[position].isUploading = false
                    messages[position].progress = progress
                    notifyItemChanged(position)
                }
            }
        }

    }
}