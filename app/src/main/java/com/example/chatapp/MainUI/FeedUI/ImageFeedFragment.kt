package com.example.chatapp.MainUI.FeedUI

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentImageFeedBinding


class ImageFeedFragment : Fragment() {
    companion object{
        private val ARG_URL = "url"

        fun newInstance(url:String): ImageFeedFragment {
            val args = Bundle()
            args.putString(ARG_URL,url)
            val fragment = ImageFeedFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var binding:FragmentImageFeedBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImageFeedBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val url = arguments?.getString(ARG_URL)
        Glide.with(requireContext()).load(url).placeholder(R.drawable.image).into(binding.feedPostImageItemPhoto)
    }


}