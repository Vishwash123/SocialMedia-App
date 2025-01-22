package com.example.chatapp.MainUI.ChatsUi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.Models.User
import com.example.chatapp.R
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.ViewModels.FriendViewModel
import com.example.chatapp.databinding.FragmentNewChatBinding
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewChat : Fragment() {
    private val friendViewModel:FriendViewModel by viewModels()
    private lateinit var binding:FragmentNewChatBinding
    private lateinit var currentUser:FirebaseUser
    private lateinit var newChatRvAdapter: NewChatRvAdapter

    private val friendsList = mutableListOf<User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNewChatBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUser = FirebaseService.firebaseAuth.currentUser!!
        binding.newChatRv.layoutManager = LinearLayoutManager(requireContext())
        newChatRvAdapter = NewChatRvAdapter(
            requireContext(),
            friendsList
        ) { userId ->
            val bundle = Bundle()
            bundle.putString("userId", userId)
            val chatRoomFragments = ChatRoom()
            chatRoomFragments.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainuiFragContainer, chatRoomFragments)
                .addToBackStack(null)
                .commit()
        }
        binding.newChatRv.adapter = newChatRvAdapter
        loadFriendsList()

        friendViewModel.friendList.observe(viewLifecycleOwner){friends->
            friendsList.clear()
            friendsList.addAll(friends)
            newChatRvAdapter.notifyDataSetChanged()
        }

    }

    private fun loadFriendsList(){
        friendViewModel.loadFriendsList(currentUser.uid)
    }

}