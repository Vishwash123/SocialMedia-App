package com.example.chatapp.MainUI.ProfileUI

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.ViewModels.UserViewModel
import com.example.chatapp.databinding.FragmentProfilePicPreviewBinding


class ProfilePicPreview : Fragment() {

    private lateinit var binding: FragmentProfilePicPreviewBinding
    private val userViewModel:UserViewModel by  activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfilePicPreviewBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.profilePicPreviewProgressBar.visibility = View.GONE
        val uriString = arguments?.getString("uri")
        val uri = Uri.parse(uriString)
        binding.profilePicPreviewPhoto.setImageURI(uri)

        binding.profilePicPreviewCancel.setOnClickListener{
            parentFragmentManager.popBackStack()
        }
        binding.profilePicPreviewDone.setOnClickListener{
            binding.profilePicPreviewProgressBar.visibility = View.VISIBLE
            userViewModel.updateProfilePic(requireContext(),FirebaseService.firebaseAuth.currentUser!!.uid,uri)
            userViewModel.profilePicProgress.observe(viewLifecycleOwner){progress->
                if(progress in 0..100){
                    binding.profilePicPreviewProgressBar.visibility = View.VISIBLE
                    binding.profilePicPreviewProgressBar.progress = progress
                }
                else{
                    binding.profilePicPreviewProgressBar.visibility = View.GONE
                    if(progress==-1){
                        parentFragmentManager.popBackStack()
                    }
                }
            }

        }
    }


}