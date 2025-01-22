package com.example.chatapp.MainUI.FeedUI

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.chatapp.databinding.FragmentVideoBinding


class VideoFragment : Fragment() {
    companion object{
        private val ARG_URI = "uri"

        fun newInstance(uri: Uri): VideoFragment {
            val args = Bundle()
            args.putParcelable("uri",uri)
            val fragment = VideoFragment()
            fragment.arguments = args
            return fragment
        }
    }
    private lateinit var binding:FragmentVideoBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentVideoBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val uri = arguments?.getParcelable<Uri>(ARG_URI)
        Log.d("video frag xxxo","$uri")
        val playerView = binding.postVideoItemPlayer
        val player = ExoPlayer.Builder(requireContext()).build()
        playerView.player = player
        val mediaItem = MediaItem.fromUri(uri!!)
        player.setMediaItem(mediaItem)
        player.prepare()

    }


}