package com.example.chatapp.MainUI

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.chatapp.MainActivity
import com.example.chatapp.R
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.ViewModels.UserViewModel
import com.example.chatapp.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Profile : Fragment() {
    private lateinit var binding:FragmentProfileBinding
    private val userViewModel:UserViewModel by viewModels()
    private lateinit var currentUser:FirebaseUser

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
        binding.currentUserName.text = currentUser.displayName
        binding.logoutButton.setOnClickListener{
            userViewModel.signOut()
            startActivity(Intent(requireContext(),MainActivity::class.java))


        }
    }

}