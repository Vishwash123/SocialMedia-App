package com.example.chatapp.Authentication

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.example.chatapp.MainActivity2
import com.example.chatapp.Models.User
import com.example.chatapp.R
import com.example.chatapp.Utilities.AuthUtils
import com.example.chatapp.ViewModels.UserViewModel
import com.example.chatapp.databinding.FragmentSignUpBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    private val STORAGE_PERMISSION_REQUEST_CODE = 1001
    private val userViewModel:UserViewModel by viewModels()
    private lateinit var binding:FragmentSignUpBinding
    private var signUpImageUri:Uri? = null

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSignUpBinding.inflate(inflater,container,false);
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.progressBarLayout.visibility = View.GONE

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                signUpImageUri = result.data?.data
                binding.profileImage.setImageURI(signUpImageUri)
                Log.d("SignUpFragment", "Image URI: $signUpImageUri")
            } else {
                Log.d("SignUpFragment", "Image selection failed or was canceled.")
            }
        }

        binding.uploadButton.setOnClickListener{

            requestStoragePermissionOrOpenImagePicker()
        }

        binding.signInText.setOnClickListener(){
            loadLogInFragment()
        }

        binding.signupButton.setOnClickListener(){
            val username = binding.usernameInput.text.toString()
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()

            signUpUser(username,email,password)
        }

        userViewModel.signUpResult.observe(viewLifecycleOwner){result->
            result.onSuccess { firebaseUser->
                Toast.makeText(requireContext(),"Sign Up Successful",Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), MainActivity2::class.java)
                intent.putExtra(firebaseUser?.displayName.toString(),firebaseUser?.email.toString())
                startActivity(intent)
            }.onFailure { exception ->
                Toast.makeText(requireContext(),"Sign Up Unsuccessful",Toast.LENGTH_SHORT).show()

            }
        }

        userViewModel.overallProgress.observe(viewLifecycleOwner){progress->
            Log.d("progress","the current progress is $progress")
            if (progress in 1..99) {
                // Show progress bar layout and text
                binding.progressBarLayout.visibility = View.VISIBLE
                binding.progressText.visibility = View.VISIBLE
                binding.progressBar.visibility = View.VISIBLE
            } else {
                // Update progress bar with the current progress value
                binding.progressBar.progress = progress
                binding.progressText.text = "Sign Up in progress...${progress}% complete"

                // Hide progress bar layout once progress reaches 100%
                if (progress == 100) {
                    binding.progressBarLayout.visibility = View.GONE
                    binding.progressText.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                }
            }

        }

    }

    private fun signUpUser(username: String, email: String, password: String) {
        if(isValidInput(username,email,password)){
            val user = User(name = username, email = email)
            userViewModel.signUp(email,password,user,signUpImageUri,requireContext())
        }

    }

    private fun requestStoragePermissionOrOpenImagePicker() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        Log.d(TAG, "Checking storage permission for: $permission")

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Storage permission granted, opening image picker.")
            openImagePicker()
        } else {
            Log.d(TAG, "Storage permission not granted, requesting permission.")
            requestPermissions(arrayOf(permission), STORAGE_PERMISSION_REQUEST_CODE)
        }
    }

    private fun checkStoragePermissionAndOpenPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 (API level 30) and above
            if (Environment.isExternalStorageManager()) {
                // Permission is granted, open image picker
                openImagePicker()
            } else {
                // Request "MANAGE_EXTERNAL_STORAGE" permission
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent, STORAGE_PERMISSION_REQUEST_CODE)
            }
        } else {
            // For lower versions (Android 10 and below)
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, open image picker
               openImagePicker()
            } else {
                // Request permission
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
            }
        }
    }

    private fun isValidInput(username: String, email: String, password: String): Boolean {
        var isValid = true
        binding.usernameInput.error = null
        binding.emailInput.error = null
        binding.passwordInput.error = null
        if(username.isEmpty()){
            binding.usernameInput.error = "This field is required"
            isValid = false
        }
        if(email.isEmpty()){
            binding.emailInput.error = "This field is required"
            isValid = false
        }
        if(password.isEmpty()){
            binding.passwordInput.error = "This field is required"
            isValid = false
        }

        return isValid
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==1 && resultCode == Activity.RESULT_OK && data!=null && data.data!=null){
            signUpImageUri = data.data
            Log.d("Url","uri is : $signUpImageUri")
            binding.profileImage.setImageURI(signUpImageUri)
        }
    }

    private fun loadLogInFragment(){
        val logInFragment = LoginFragment()
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container,logInFragment).addToBackStack(null).commit()
    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.d("SignUpFragment", "onRequestPermissionsResult called.")

        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open image picker
                Log.d("SignUpFragment", "Permission granted in result, opening image picker.")
                openImagePicker()
            } else {
                Log.d("SignUpFragment", "Permission denied.")
                // Permission denied, check if it's a permanent denial
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Temporary denial, we can try again later
                    Toast.makeText(requireContext(), "Storage permission is required to select an image.", Toast.LENGTH_SHORT).show()
                } else {
                    // Permanent denial, user checked "Don't ask again"
                    Toast.makeText(requireContext(), "Storage permission is permanently denied. Please enable it in settings.", Toast.LENGTH_LONG).show()
                    // Optionally, you can direct the user to app settings:
                    // openAppSettings()
                }
            }
        }
    }

    fun openImagePicker(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select an Image"), 1)
    }

}