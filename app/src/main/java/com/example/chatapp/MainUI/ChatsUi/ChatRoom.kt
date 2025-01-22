package com.example.chatapp.MainUI.ChatsUi

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatapp.Models.Calltype
import com.example.chatapp.Models.Message
import com.example.chatapp.Models.User
import com.example.chatapp.MainUI.ProfileUI.OtherUserProfile
import com.example.chatapp.R
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.Utilities.Util
import com.example.chatapp.Utilities.ZegoUtil
import com.example.chatapp.ViewModels.CallsViewModel
import com.example.chatapp.ViewModels.ChatsViewModel
import com.example.chatapp.ViewModels.UserViewModel
import com.example.chatapp.databinding.FragmentChatRoomBinding
import com.google.firebase.auth.FirebaseUser
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ChatRoom : Fragment() {
    private val userViewModel:UserViewModel by activityViewModels()
    private val chatsViewModel:ChatsViewModel by activityViewModels()
    private val callsViewModel:CallsViewModel by activityViewModels()
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
        chatRoomRvAdapter = ChatRoomRvAdapter(requireContext(), messagesList){url->
            loadVideoPlayFragment(url)
        }
        binding.messagesRv.adapter = chatRoomRvAdapter
        binding.attachFiles.setOnClickListener{



            val bottomSheetFragment = ItemListDialogFragment.newInstance(chatId,otherUserId!!,requireContext())


            bottomSheetFragment.show(parentFragmentManager,"ItemListDialogFragment")
        }

        binding.sendButton.setOnClickListener{
            val messageText = binding.chatRoomMessageTyper.text.toString()
            sendMessage(messageText)
            binding.chatRoomMessageTyper.text.clear()
        }
        binding.chatRoomBackButton.setOnClickListener{
            Util.loadFragment(parentFragmentManager, Chats(),true)
        }
        binding.chatRoomVoiceCall.setOnClickListener(View.OnClickListener {
            binding.chatRoomVoiceCall.setIsVideoCall(false)
            binding.chatRoomVoiceCall.resourceID = "chat_app_call"
            binding.chatRoomVoiceCall.setInvitees(mutableListOf(ZegoUIKitUser(otherUserId,otherUser.name)))
            val callId = "${FirebaseService.firebaseAuth.currentUser!!.uid}_${otherUserId}_${System.currentTimeMillis()}_${Calltype.AUDIO.toString()}"
            ZegoUtil.currentActiveCallId = callId
            callsViewModel.addCall(callId,FirebaseService.firebaseAuth.currentUser!!.uid,
                otherUserId!!,
                Calltype.AUDIO)

        })

        binding.chatRoomVideoCall.setOnClickListener(View.OnClickListener {
            binding.chatRoomVideoCall.setIsVideoCall(true)
            binding.chatRoomVideoCall.resourceID = "chat_app_call"

            binding.chatRoomVideoCall.setInvitees(mutableListOf(ZegoUIKitUser(otherUserId,otherUser.name)))
            val callId = "${FirebaseService.firebaseAuth.currentUser!!.uid}_${otherUserId}_${System.currentTimeMillis()}_${Calltype.VIDEO.toString()}"

            ZegoUtil.currentActiveCallId = callId
            callsViewModel.addCall(callId,FirebaseService.firebaseAuth.currentUser!!.uid,
                otherUserId!!,Calltype.VIDEO)

        })

        binding.chatRoomName.setOnClickListener{
            openOtherUserProfile()
        }
        binding.chatRoomProfilePic.setOnClickListener{
            openOtherUserProfile()
        }


        setUpUserObserver()
        fetchUserData(otherUserId)
        setUpMessageObserver()
        fetchMessageData(chatId)
        chatsViewModel.uploadProgress.observe(viewLifecycleOwner){progressMap->
            Log.d("updating rv xxo","yeah")
            chatRoomRvAdapter.updateUploadProgress(progressMap)
        }

    }

    private fun openOtherUserProfile() {
        val fragment = OtherUserProfile()
        val bundle = Bundle()
        bundle.putString("id",otherUserId)
        fragment.arguments = bundle
        parentFragmentManager.beginTransaction().replace(R.id.mainuiFragContainer,fragment).addToBackStack(null).commit()
    }

    private fun loadVideoPlayFragment(url: String) {
        val videoFragment = VideoPlaying()
        val bundle = Bundle()
        bundle.putString("url",url)
        videoFragment.arguments = bundle
        parentFragmentManager.beginTransaction().replace(R.id.mainuiFragContainer,videoFragment).addToBackStack(null).commit()
    }

    private fun sendMessage(messageText: String) {
        chatsViewModel.sendMessage(requireContext(),currentUser.uid,otherUserId!!,messageText, chatId =  chatId)
    }

    private fun setUpMessageObserver() {
        chatsViewModel.getMessagesLiveData(chatId).observe(viewLifecycleOwner){messages->
            Log.d("in frgament messages : ","${messages.size}")
            messagesList.clear()
            messagesList.addAll(messages)
            Log.d("in frgament messagesList : ","${messagesList.size}")

            chatRoomRvAdapter.notifyDataSetChanged()
            binding.messagesRv.scrollToPosition(chatRoomRvAdapter.itemCount - 1)
        }
        chatsViewModel.fetchChats(chatId)
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
                    otherUser = user
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