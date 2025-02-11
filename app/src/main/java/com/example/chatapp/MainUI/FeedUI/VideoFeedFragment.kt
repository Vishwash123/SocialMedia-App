package com.example.chatapp.MainUI.FeedUI

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentVideoFeedBinding


class VideoFeedFragment : Fragment() {

     companion object{
         private val ARG_VIDEO_URL = "url"

         fun newInstance(url:String):VideoFeedFragment{
             val bundle = Bundle()
             bundle.putString(ARG_VIDEO_URL,url)
             val fragment = VideoFeedFragment()
             fragment.arguments = bundle
             return fragment
         }
     }

    private lateinit var binding:FragmentVideoFeedBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =FragmentVideoFeedBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val url = arguments?.getString(ARG_VIDEO_URL)
        val playerView = binding.feePostVideoItemPlayer
        val player = ExoPlayer.Builder(requireContext()).build()
        playerView.player = player
        val mediaItem = MediaItem.fromUri(Uri.parse(url))
        player.setMediaItem(mediaItem)
        player.repeatMode = Player.REPEAT_MODE_ONE
        player.prepare()
        player.playWhenReady = true
    }


}