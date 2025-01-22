package com.example.chatapp.MainUI.CallsUI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.Models.Calltype
import com.example.chatapp.Models.User
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.Utilities.ZegoUtil
import com.example.chatapp.ViewModels.CallsViewModel
import com.example.chatapp.ViewModels.FriendViewModel
import com.example.chatapp.databinding.FragmentNewCallsBinding
import com.zegocloud.uikit.service.defines.ZegoUIKitUser


class NewCalls : Fragment() {
    private val friendViewModel:FriendViewModel by activityViewModels()
    private val callsViewModel:CallsViewModel by activityViewModels()
    private lateinit var binding:FragmentNewCallsBinding
    private lateinit var newCallsRvAdapter: NewCallsRvAdapter

    private val list = mutableListOf<User>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNewCallsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.newCallRv.layoutManager = LinearLayoutManager(requireContext())
        newCallsRvAdapter = NewCallsRvAdapter(requireContext(),list,
            onAudioClicked = {button,user->
                button.setIsVideoCall(false)
                button.resourceID = "chat_app_call"
                button.setInvitees(mutableListOf(ZegoUIKitUser(user.id,user.name)))
                val callId = "${FirebaseService.firebaseAuth.currentUser!!.uid}_${user.id}_${System.currentTimeMillis()}_${Calltype.AUDIO.toString()}"

                callsViewModel.addCall(callId,FirebaseService.firebaseAuth.currentUser!!.uid,user.id,
                    Calltype.AUDIO)
                ZegoUtil.currentActiveCallId = callId

                popFragment()
            },
            onVideoClicked = {button,user->
                button.setIsVideoCall(true)
                button.resourceID = "chat_app_call"
                button.setInvitees(mutableListOf(ZegoUIKitUser(user.id,user.name)))
                val callId = "${FirebaseService.firebaseAuth.currentUser!!.uid}_${user.id}_${System.currentTimeMillis()}_${Calltype.VIDEO.toString()}"
                ZegoUtil.currentActiveCallId = callId

                callsViewModel.addCall(callId,FirebaseService.firebaseAuth.currentUser!!.uid,user.id,Calltype.VIDEO)

                popFragment()
            }
        )

        binding.newCallRv.adapter = newCallsRvAdapter

        loadFriendList()
        friendViewModel.friendList.observe(viewLifecycleOwner){friends->
            list.clear()
            list.addAll(friends)
            newCallsRvAdapter.notifyDataSetChanged()
        }



    }

    private fun loadFriendList() {
        friendViewModel.loadFriendsList(FirebaseService.firebaseAuth.currentUser!!.uid)
    }

    private fun popFragment() {
        parentFragmentManager.popBackStack()
    }


}