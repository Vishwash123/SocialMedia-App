package com.example.chatapp.Authentication

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import com.example.chatapp.MainActivity2
import com.example.chatapp.R
import com.example.chatapp.ViewModels.UserViewModel
import com.example.chatapp.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var binding: FragmentLoginBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener{
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            userViewModel.login(email,password)
        }

        binding.signUpText.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container,SignUpFragment()).commit()
        }


        userViewModel.logInResult.observe(viewLifecycleOwner){result ->
            result.onSuccess { firebaseUser ->
                // Login successful, navigate to next activity or fragment
                Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), MainActivity2::class.java)
                intent.putExtra("userEmail", firebaseUser?.email)  // Pass any data you need to the next activity
                startActivity(intent)
                requireActivity().finish()  // Optionally finish the current activity (if you want to close the login screen)
            }.onFailure { exception ->
                // Login failed, show error message
                Toast.makeText(requireContext(), "Login Unsuccessful: ${exception.message}", Toast.LENGTH_SHORT).show()
            }

        }
    }



}