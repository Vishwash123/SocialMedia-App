package com.example.chatapp.MainUI.ChatsUi

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.ViewModels.ChatsViewModel
import com.example.chatapp.databinding.FragmentPreviewBinding
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Preview : Fragment() {
    private val chatsViewModel:ChatsViewModel by activityViewModels()
    private lateinit var binding:FragmentPreviewBinding
    private lateinit var otherUserId: String
    private lateinit var chatId: String
    private lateinit var currentUser:FirebaseUser
    private lateinit var attachmentType:String
    private lateinit var attachmentUri:Uri

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentPreviewBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        attachmentType = arguments?.getString("type")!!
        attachmentUri = arguments?.getParcelable<Uri>("uri")!!
        Log.d("yeah","$attachmentUri")
        Log.d("yeaht","$attachmentType")
        otherUserId = arguments?.getString("otherUserid")!!
        chatId = arguments?.getString("chatId")!!
        currentUser = FirebaseService.firebaseAuth.currentUser!!
        val temporaryId= "${currentUser.uid}_${otherUserId}_${System.currentTimeMillis()}"
        when(attachmentType){
            "image"->{
                binding.previewVideo.visibility = View.GONE
                binding.previewPhoto.visibility = View.VISIBLE
                Glide.with(requireContext()).load(attachmentUri).placeholder(R.drawable.ic_person_placeholder).into(binding.previewPhoto)
            }
            "video"->{
                binding.previewVideo.visibility = View.VISIBLE
                binding.previewPhoto.visibility = View.GONE
                val player = ExoPlayer.Builder(requireContext()).build()
                binding.previewVideo.player = player
                val mediaItem = MediaItem.fromUri(attachmentUri)
                player.setMediaItem(mediaItem)
                player.prepare()


            }
        }


        binding.closeButton.setOnClickListener{
            parentFragmentManager.popBackStack()
        }
        binding.previewSendButton.setOnClickListener{
            val caption = binding.previewMessageTyper.text.toString()
            Log.d("other id check","$otherUserId")
            chatsViewModel.sendMessage(requireContext().applicationContext,currentUser.uid,otherUserId,caption,attachmentUri,temporaryId, chatId = chatId )
                    parentFragmentManager.popBackStack()



        }


    }

    override fun onStart() {
        super.onStart()
        Log.d("shit","started")
    }

}