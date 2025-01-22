package com.example.chatapp.MainUI.CallsUI

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.Models.Call
import com.example.chatapp.Models.Calltype
import com.example.chatapp.Models.User
import com.example.chatapp.Utilities.Util
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.Utilities.ZegoUtil
import com.example.chatapp.ViewModels.CallsViewModel
import com.example.chatapp.databinding.FragmentCallsBinding
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class Calls : Fragment() {
    private val callsViewModel:CallsViewModel by activityViewModels()

    private lateinit var binding:FragmentCallsBinding
    private lateinit var callsRvAdapter: CallsRvAdapter
    private val calls = mutableListOf<Call>()
    private val userMap = mutableMapOf<String,Pair<User?,User?>>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCallsBinding.inflate(inflater,container,false)
        return binding.root
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.newCallButton.setOnClickListener{
            Util.loadFragment(parentFragmentManager, NewCalls(),true)
        }

        binding.callsRv.layoutManager = LinearLayoutManager(requireContext())
        callsRvAdapter = CallsRvAdapter(requireContext(),calls,userMap,
            onAudioClick = {button,user->
                button.setIsVideoCall(false)
                button.resourceID = "chat_app_call"

                button.setInvitees(mutableListOf(ZegoUIKitUser(user.id,user.name)))
                val callId = "${FirebaseService.firebaseAuth.currentUser!!.uid}_${user.id}_${System.currentTimeMillis()}_${Calltype.AUDIO.toString()}"
                ZegoUtil.currentActiveCallId = callId
                callsViewModel.addCall(callId,FirebaseService.firebaseAuth.currentUser!!.uid,user.id,Calltype.AUDIO)
            },
            onVideoClick = {button,user->
                button.setIsVideoCall(true)
                button.resourceID = "chat_app_call"
                button.setInvitees(mutableListOf(ZegoUIKitUser(user.id,user.name)))
                val callId = "${FirebaseService.firebaseAuth.currentUser!!.uid}_${user.id}_${System.currentTimeMillis()}_${Calltype.VIDEO.toString()}"
                ZegoUtil.currentActiveCallId = callId

                callsViewModel.addCall(callId,FirebaseService.firebaseAuth.currentUser!!.uid,user.id,Calltype.VIDEO)

            }
        )
        binding.callsRv.adapter = callsRvAdapter

        callsViewModel.fetchCallsForUser(FirebaseService.firebaseAuth.currentUser!!.uid)

        // Observe the call list
        callsViewModel.callsList.observe(viewLifecycleOwner) { calls ->
            this.calls.clear()
            this.calls.addAll(calls)
            Log.d("calls xxxo","${calls.size}")
            callsRvAdapter.notifyDataSetChanged()
        }

        // Observe the call-user map (caller and receiver details)
        callsViewModel.callUserMap.observe(viewLifecycleOwner) { callUserMap ->
            this.userMap.clear()
            this.userMap.putAll(callUserMap)
            Log.d("calls xxxo","${callUserMap.size}")
            callsRvAdapter.notifyDataSetChanged()
        }





//        loadCalls()
//        callsViewModel.callsList.observe(viewLifecycleOwner){list->
//            calls.clear()
//            calls.addAll(list)
//            userMap.clear()
//            val userIds = list.flatMap { call -> listOf(call.caller, call.receiver) }.distinct()
//
//            userViewModel.fetchUsersData(userIds)  // Pass all user IDs to fetch data
//
//            userViewModel.userData.observe(viewLifecycleOwner) { users ->
//                users.forEach { user ->
//                    // Use the user data as needed (e.g., populate the map)
//                    userMap[call.callId] = listOf(user!!)
//                }
//                callsRvAdapter.notifyDataSetChanged()
//            }
//            list.forEach{call->
//                Log.d("calls xxxo","${call.caller} and ${call.receiver}")
//
//                val caller:User = userViewModel.getUserAndReturn(call.caller)!!
//                val receiver:User = userViewModel.getUserAndReturn(call.receiver)!!
//                userMap.put(call.callId, listOf(caller,receiver))
//            }
//            Log.d("calls xxxo","${calls.size} & ${userMap.size}")
//            callsRvAdapter.notifyDataSetChanged()
        //}
//

    }

//    private fun loadCalls() {
//        callsViewModel.fetchCalls(FirebaseService.firebaseAuth.currentUser!!.uid)
//    }


}