package com.example.chatapp.MainUI.ProfileUI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.chatapp.Models.Post
import com.example.chatapp.R
import com.example.chatapp.ViewModels.UserViewModel
import com.example.chatapp.databinding.FragmentOtherUserProfileBinding


class OtherUserProfile : Fragment() {
    private lateinit var binding: FragmentOtherUserProfileBinding
    private val userViewModel:UserViewModel by activityViewModels()
    private lateinit var callback: OnBackPressedCallback
    private val postList = mutableListOf<Post>()
    private val postIdsList = mutableListOf<String>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOtherUserProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle back press within the fragment
                // For example, close a dialog or perform fragment-specific actions
                // If no specific action is required, let the Activity handle it
                if (requireActivity().supportFragmentManager.backStackEntryCount > 0) {
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    requireActivity().finish()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        val userId = arguments?.getString("id")
        setUpObserver(userId!!)
    }

    private fun setUpObserver(userId:String) {
        userViewModel.fetchAndReturnUser(userId)
        userViewModel.otherProfileUiState.observe(viewLifecycleOwner){state->
            binding.otherProfileBio.text = state.bio
            binding.otherProfileName.text = state.name
            binding.otherProfilePostCount.text = state.posts.toString()
            binding.otherProfileFriendCount.text = state.friends.toString()
            if(state.posts==0){
                binding.otherProfilePostsRv.visibility=View.GONE
                binding.otherProfileNoPostsTv.visibility = View.VISIBLE
                binding.catFace.visibility = View.VISIBLE
            }
            else{
                state.postIds.forEach{
                    postIdsList.add(it)
                }
                //fetchPostsAndUpdate
                binding.otherProfilePostsRv.visibility=View.VISIBLE
                binding.otherProfileNoPostsTv.visibility = View.GONE
                binding.catFace.visibility = View.GONE
            }
            Glide.with(requireContext()).load(state.profilePic).placeholder(R.drawable.profile).into(binding.otherProfilePhoto)
        }
    }





}