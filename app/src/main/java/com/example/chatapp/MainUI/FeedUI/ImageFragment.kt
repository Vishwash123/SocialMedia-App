package com.example.chatapp.MainUI.FeedUI

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentImageBinding


class ImageFragment : Fragment() {
    companion object{
        private val ARG_URI = "uri"

        fun newInstance(uri: Uri): ImageFragment {
            val args = Bundle()
            args.putParcelable(ARG_URI,uri)
            val fragment = ImageFragment()
            fragment.arguments = args
            return fragment
        }
    }
    private lateinit var binding:FragmentImageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentImageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uri = arguments?.getParcelable<Uri>(ARG_URI)
        val imageView = binding.postImageItemPhoto
        Glide.with(requireContext()).load(uri).placeholder(R.drawable.image).into(imageView)


    }


}