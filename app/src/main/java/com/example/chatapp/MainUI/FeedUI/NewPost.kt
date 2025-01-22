package com.example.chatapp.MainUI.FeedUI

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.chatapp.Utilities.AttachmentUtils
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.ViewModels.PostViewModel
import com.example.chatapp.databinding.FragmentNewPostBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class NewPost : Fragment() {
    private lateinit var binding: FragmentNewPostBinding
    private val postViewModel:PostViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNewPostBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.newPostProgressBar.visibility = View.GONE
        binding.newPostProgressText.visibility = View.GONE
        val receivedUris = arguments?.getParcelableArrayList<Uri>("uris")
        Log.d("new post xxo","${receivedUris.toString()}")
        binding.newPostVp.adapter = NewPostVPAdapter(requireContext(),childFragmentManager,lifecycle,receivedUris!!.toList())

        postViewModel.postUploadProgress.observe(viewLifecycleOwner){progress->
            Log.d("new posts observer xxo","$progress")

           if(progress!=null && progress>=0 && progress<=receivedUris.size) {
//                Log.d("new posts observer xxo","$progress")
                binding.newPostProgressBar.visibility = View.VISIBLE
                binding.newPostProgressText.visibility = View.VISIBLE

                binding.newPostProgressBar.progress = progress
                binding.newPostProgressText.text = "Uploading file ${progress} of ${receivedUris.size}"
            }
            else {

               if(progress==-1) {
                   binding.newPostProgressBar.visibility = View.GONE
                   binding.newPostProgressText.visibility = View.GONE
                   postViewModel.reset()
                   parentFragmentManager.popBackStack()
               }
           }


        }
        binding.newPostDoneButton.setOnClickListener{
            val caption = binding.newPostCaptionTyper.text.toString()
            val postType = AttachmentUtils.getPostType(receivedUris,requireContext())
            postViewModel.createPost(requireContext(),FirebaseService.firebaseAuth.currentUser!!.uid,receivedUris,caption,postType)
        }

    }

}