package com.example.chatapp.MainUI.ProfileUI

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.chatapp.MainActivity
import com.example.chatapp.R
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.ViewModels.UserViewModel
import com.example.chatapp.databinding.FragmentProfileBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Profile : Fragment() {

    private lateinit var binding:FragmentProfileBinding
    private val userViewModel:UserViewModel by activityViewModels()
    private lateinit var currentUser:FirebaseUser
    private lateinit var profilePostsRvAdapter: ProfilePostsRvAdapter
    private var profilePic:String=""
    private val STORAGE_PERMISSION_REQUEST_CODE = 1002
    private lateinit var photoLauncher:ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUser = FirebaseService.firebaseAuth.currentUser!!
        userViewModel.fetchProfilUIState(currentUser.uid)
        userViewModel.profileUiState.observe(viewLifecycleOwner){state->
            binding.profileName.text = state.name
            binding.profileFriendCount.text = state.friends.toString()
            binding.profilePostCount.text = state.posts.toString()
            binding.profileBio.text = state.bio
            if(state.posts==0){
                binding.profilePostsRv.visibility=View.GONE
                binding.profileNoPostsTv.visibility = View.VISIBLE
                binding.catFace.visibility = View.VISIBLE
            }
            else{
                binding.profilePostsRv.visibility=View.VISIBLE
                binding.profileNoPostsTv.visibility = View.GONE
                binding.catFace.visibility = View.GONE
            }
            profilePic = state.profilePic
            Glide.with(requireContext()).load(state.profilePic).placeholder(R.drawable.profile).into(binding.profilePhoto)
        }

        photoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()){uri->
            uri?.let {
                val photoUri = it;
                openProfilePreviewFragment(photoUri)
            }
        }
        binding.profileEditBio.setOnClickListener{
            showBioAlertDialog()
        }

        binding.profileChangePicText.setOnClickListener{
            photoLauncher.launch("image/*")
        }

        val list = mutableListOf<Int>()
        for(i in 1..15){
            list.add(R.drawable.demo_image)
        }

        binding.profileLogoutButton.setOnClickListener{
            logout()
        }


        binding.profilePhoto.setOnClickListener{
            showProfilePhoto()
        }


        binding.profilePostsRv.layoutManager = GridLayoutManager(requireContext(),3)
        profilePostsRvAdapter = ProfilePostsRvAdapter(requireContext(),list)
        binding.profilePostsRv.adapter = profilePostsRvAdapter

    }

    private fun openProfilePreviewFragment(newUri: Uri?) {
        val fragment = ProfilePicPreview()
        val bundle = Bundle()
        bundle.putString("uri",newUri.toString())
        fragment.arguments = bundle
        parentFragmentManager.beginTransaction().replace(R.id.mainuiFragContainer,fragment).addToBackStack(null).commit()

    }

    private fun showProfilePhoto() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.profile_photo_dialog,null)
        val builder = AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(true)
        val alertDialog = builder.create()

        val photo = dialogView.findViewById<ImageView>(R.id.profilePhotoDialogPhoto)
        Glide.with(requireContext()).load(profilePic).placeholder(R.drawable.ic_person_placeholder).into(photo)
        alertDialog.show()
    }

    @SuppressLint("ResourceType")
    private fun showBioAlertDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.edit_bio_alert_dialog,null)
        val builder = AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(true)
        val alertDialog = builder.create()
        val bioText = binding.profileBio.text

        val editBox = dialogView.findViewById<TextInputEditText>(R.id.editBioEditText)
        editBox.setText(bioText)
        val saveButton = dialogView.findViewById<CardView>(R.id.editBioSaveButton)
        val cancelButton = dialogView.findViewById<CardView>(R.id.editBioCancelButton)

        saveButton.setOnClickListener{
            val newBio = editBox.text.toString()
            userViewModel.updateBio(FirebaseService.firebaseAuth.currentUser!!.uid,newBio)
            alertDialog.dismiss()
        }

        cancelButton.setOnClickListener{
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun logout(){
        FirebaseService.firebaseAuth.signOut()
        val intent = Intent(requireContext(),MainActivity::class.java)
        startActivity(intent)
    }








}