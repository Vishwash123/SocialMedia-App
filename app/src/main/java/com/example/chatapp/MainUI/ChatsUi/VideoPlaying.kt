package com.example.chatapp.MainUI.ChatsUi

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentVideoPlayingBinding

class VideoPlaying : Fragment() {
    private lateinit var binding:FragmentVideoPlayingBinding
    private lateinit var player:ExoPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentVideoPlayingBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val url = arguments?.getString("url")
        val playerView = binding.videoPlayingPlayer
        player = ExoPlayer.Builder(requireContext()).build()
        playerView.player = player
        val mediaItem = MediaItem.fromUri(Uri.parse(url))
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        binding.videoPlayingBackButton.setOnClickListener{

            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }


}