package com.example.chatapp.MainUI

import android.content.ClipData.Item
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatapp.MainUI.ChatsUi.ChatRoomRvAdapter
import com.example.chatapp.MainUI.ChatsUi.ItemListDialogFragment
import com.example.chatapp.Models.Message
import com.example.chatapp.Models.User
import com.example.chatapp.R
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.ViewModels.ChatsViewModel
import com.example.chatapp.ViewModels.UserViewModel
import com.example.chatapp.databinding.FragmentChatRoomBinding
import com.google.firebase.analytics.FirebaseAnalytics.UserProperty
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ChatRoom : Fragment() {
    private val userViewModel:UserViewModel by viewModels()
    private val chatsViewModel:ChatsViewModel by viewModels()
    private lateinit var binding:FragmentChatRoomBinding
    private var otherUserId:String?=null
    private lateinit var otherUser:User
    private lateinit var currentUser: FirebaseUser
    private lateinit var chatRoomRvAdapter: ChatRoomRvAdapter
    private val messagesList = mutableListOf<Message>()
    private lateinit var chatId:String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChatRoomBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUser = FirebaseService.firebaseAuth.currentUser!!
        otherUserId = arguments?.getString("userId")
        chatId = if (currentUser.uid < otherUserId!!) "${currentUser.uid}_${otherUserId!!}" else "${otherUserId!!}_${currentUser.uid}"

        binding.messagesRv.layoutManager = LinearLayoutManager(requireContext()).apply {
            orientation=LinearLayoutManager.VERTICAL
            stackFromEnd = true

        }
        chatRoomRvAdapter = ChatRoomRvAdapter(requireContext(), messagesList)
        binding.messagesRv.adapter = chatRoomRvAdapter
        binding.attachFiles.setOnClickListener{



            val bottomSheetFragment = ItemListDialogFragment.newInstance(otherUserId!!,requireContext())


            bottomSheetFragment.show(parentFragmentManager,"ItemListDialogFragment")
        }

        binding.sendButton.setOnClickListener{
            val messageText = binding.chatRoomMessageTyper.text.toString()
            sendMessage(messageText)
            binding.chatRoomMessageTyper.text.clear()
        }
        binding.chatRoomBackButton.setOnClickListener{
            parentFragmentManager.popBackStack()
        }

        setUpUserObserver()
        fetchUserData(otherUserId)
        setUpMessageObserver()
        fetchMessageData(chatId)
    }

    private fun sendMessage(messageText: String) {
        chatsViewModel.sendMessage(requireContext(),currentUser.uid,otherUserId!!,messageText)
    }

    private fun setUpMessageObserver() {
        chatsViewModel.messages.observe(viewLifecycleOwner){messages->
            Log.d("in frgament messages : ","${messages.size}")
            messagesList.clear()
            messagesList.addAll(messages)
            Log.d("in frgament messagesList : ","${messagesList.size}")

            chatRoomRvAdapter.notifyDataSetChanged()
            binding.messagesRv.scrollToPosition(chatRoomRvAdapter.itemCount - 1)
        }
    }

    private fun fetchUserData(otherUserId: String?) {
        userViewModel.fetchUser(otherUserId!!)
    }

    private fun fetchMessageData(chatId:String){
        chatsViewModel.fetchMessages(chatId)
    }

    private fun setUpUserObserver(){

        userViewModel.fetchUserResult.observe(viewLifecycleOwner){result->
            result.onSuccess { user->
                if(user!=null){
                    updateUI(user)
                }else{
                    Toast.makeText(requireContext(),"User not found",Toast.LENGTH_SHORT).show()
                }
            }.onFailure {
                Toast.makeText(requireContext(),"Failed to load user data",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(user: User) {
        binding.chatRoomName.text = user.name
        Glide.with(requireContext())
            .load(user.profilePic)
            .placeholder(R.drawable.ic_person_placeholder)
            .into(binding.chatRoomProfilePic)

    }


}