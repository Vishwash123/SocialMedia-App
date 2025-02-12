package com.example.chatapp.MainUI.ChatsUi

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatapp.Models.Calltype
import com.example.chatapp.Models.Message
import com.example.chatapp.Models.OneToOneChat
import com.example.chatapp.Models.User
import com.example.chatapp.MainUI.ProfileUI.OtherUserProfile
import com.example.chatapp.R
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.Utilities.Util
import com.example.chatapp.Utilities.ZegoUtil
import com.example.chatapp.ViewModels.CallsViewModel
import com.example.chatapp.ViewModels.ChatsViewModel
import com.example.chatapp.databinding.FragmentChatsBinding

import com.google.firebase.auth.FirebaseUser
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Chats : Fragment() {
    private lateinit var binding:FragmentChatsBinding
    private lateinit var currentUser:FirebaseUser
    private val chatsList:MutableList<OneToOneChat> = mutableListOf<OneToOneChat>()
    private val usersMap:MutableMap<String,User> = mutableMapOf<String,User>()
    private val messagesMap:MutableMap<String,Message> = mutableMapOf<String,Message>()
    private val chatsViewModel:ChatsViewModel by activityViewModels()
    private val callsViewModel:CallsViewModel by activityViewModels()
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
        , onItemClicked = { userId ->
            val bundle = Bundle()
            bundle.putString("userId", userId)
            val chatRoomFragment = ChatRoom()
            chatRoomFragment.arguments = bundle
            loadFragment(chatRoomFragment)
        }
        ,onPhotoClicked = {user->
            showProfileDialog(user)
        })
        binding.chatsRv.adapter = chatsRvAdapter

        currentUser = FirebaseService.firebaseAuth.currentUser!!
        chatsViewModel.fetchChats(currentUser.uid)
        chatsViewModel.chatUILists.observe(viewLifecycleOwner){state->
            Log.d("chat frag xxxo","triggered observer")
            chatsList.clear()
            usersMap.clear()
            messagesMap.clear()
            chatsList.addAll(state!!.chatsList)
            usersMap.putAll(state!!.usersMap)
            messagesMap.putAll(state!!.messagesMap)
            Log.d("chats size ","${chatsList.size}")
            Log.d("users ","${usersMap}")
            chatsRvAdapter.notifyDataSetChanged()


        }


    }


    private fun showProfileDialog(user:User) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.profile_alert_dialog,null)
        val builder = AlertDialog.Builder(requireContext(),R.style.CustomAlertDialog).setView(dialogView).setCancelable(true)
        val alertDialog = builder.create()

        val name = dialogView.findViewById<TextView>(R.id.profileDialogName)
        val photo = dialogView.findViewById<ImageView>(R.id.profileDialogImage)
        val profileIcon = dialogView.findViewById<ImageView>(R.id.profileDialogProfileIcon)
        val audioCallButton:ZegoSendCallInvitationButton = dialogView.findViewById(R.id.profileDialogAudioCallIcon)
        val videoCallButton:ZegoSendCallInvitationButton = dialogView.findViewById(R.id.profileDialogVideoCallIcon)
        name.text = user!!.name


        Glide.with(requireContext()).load(user!!.profilePic).placeholder(R.drawable.ic_person_placeholder).into(photo)

        profileIcon.setOnClickListener{
            loadOtherUsersProfile(user)
            alertDialog.dismiss()
        }

        audioCallButton.setOnClickListener(View.OnClickListener {
            audioCallButton.setIsVideoCall(false)
            audioCallButton.resourceID = "chat_app_call"
            audioCallButton.setInvitees(mutableListOf(ZegoUIKitUser(user!!.id,user!!.name)))
            val callId = "${FirebaseService.firebaseAuth.currentUser!!.uid}_${user!!.id}_${System.currentTimeMillis()}_${Calltype.AUDIO.toString()}"
            ZegoUtil.currentActiveCallId = callId
            callsViewModel.addCall(callId,FirebaseService.firebaseAuth.currentUser!!.uid,
                user!!.id,
                Calltype.AUDIO)

            alertDialog.dismiss()
        })

        videoCallButton.setOnClickListener(View.OnClickListener {
            videoCallButton.setIsVideoCall(true)
            videoCallButton.resourceID = "chat_app_call"
            videoCallButton.setInvitees(mutableListOf(ZegoUIKitUser(user!!.id,user!!.name)))
            val callId = "${FirebaseService.firebaseAuth.currentUser!!.uid}_${user!!.id}_${System.currentTimeMillis()}_${Calltype.VIDEO.toString()}"
            ZegoUtil.currentActiveCallId = callId
            callsViewModel.addCall(callId,FirebaseService.firebaseAuth.currentUser!!.uid,
                user!!.id,
                Calltype.VIDEO)
            alertDialog.dismiss()
        })

        alertDialog.show()
    }

    private fun loadOtherUsersProfile(user: User) {
        val fragment = OtherUserProfile()
        val bundle = Bundle()
        bundle.putString("id",user!!.id)
        fragment.arguments = bundle
        parentFragmentManager.beginTransaction().replace(R.id.mainuiFragContainer,fragment).addToBackStack(null).commit()
    }

    private fun loadFragment(fragment: Fragment){

        parentFragmentManager.beginTransaction().replace(R.id.mainuiFragContainer,fragment).addToBackStack(null).commit()
    }


    override fun onResume() {
        super.onResume()

    }
}