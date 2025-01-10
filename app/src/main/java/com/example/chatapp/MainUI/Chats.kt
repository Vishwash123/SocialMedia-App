package com.example.chatapp.MainUI

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.MainUI.ChatsUi.NewChat
import com.example.chatapp.MainUI.MainUiRvs.ChatsRvAdapter
import com.example.chatapp.Models.ChatUIState
import com.example.chatapp.Models.Message
import com.example.chatapp.Models.OneToOneChat
import com.example.chatapp.Models.User
import com.example.chatapp.R
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.Utilities.Util
import com.example.chatapp.ViewModels.ChatsViewModel
import com.example.chatapp.databinding.FragmentChatsBinding
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Chats : Fragment() {
    private lateinit var binding:FragmentChatsBinding
    private lateinit var currentUser:FirebaseUser
    private val chatsList:MutableList<OneToOneChat> = mutableListOf<OneToOneChat>()
    private val usersMap:MutableMap<String,User> = mutableMapOf<String,User>()
    private val messagesMap:MutableMap<String,Message> = mutableMapOf<String,Message>()
    private val chatsViewModel:ChatsViewModel by viewModels()
    private lateinit var chatsRvAdapter: ChatsRvAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChatsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.newChatButton.setOnClickListener{
            loadFragment(NewChat())
        }
        binding.chatsRv.layoutManager = LinearLayoutManager(requireContext())
        val dividerItemDecoration = Util.getDividerItemDecoration(binding.chatsRv.context,(binding.chatsRv.layoutManager as LinearLayoutManager).orientation)
        binding.chatsRv.addItemDecoration(dividerItemDecoration)
        chatsRvAdapter = ChatsRvAdapter(requireContext(),chatsList , usersMap, messagesMap
        ) { userId ->
            val bundle = Bundle()
            bundle.putString("userId", userId)
            val chatRoomFragment = ChatRoom()
            chatRoomFragment.arguments = bundle
            loadFragment(chatRoomFragment)
        }
        binding.chatsRv.adapter = chatsRvAdapter

        currentUser = FirebaseService.firebaseAuth.currentUser!!
        chatsViewModel.fetchChats(currentUser.uid)
        chatsViewModel.chatUILists.observe(viewLifecycleOwner){state->
            chatsList.clear()
            usersMap.clear()
            messagesMap.clear()
            chatsList.addAll(state.chatsList)
            usersMap.putAll(state.usersMap)
            messagesMap.putAll(state.messagesMap)
            Log.d("chats size ","${chatsList.size}")
            Log.d("users ","${usersMap}")
            chatsRvAdapter.notifyDataSetChanged()


        }

    }

    private fun loadFragment(fragment: Fragment){

        parentFragmentManager.beginTransaction().replace(R.id.mainuiFragContainer,fragment).addToBackStack(null).commit()
    }


    override fun onResume() {
        super.onResume()

    }
}