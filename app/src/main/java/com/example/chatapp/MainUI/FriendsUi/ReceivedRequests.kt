package com.example.chatapp.MainUI.FriendsUi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.MainUI.FriendsUi.FriendsUiRvs.ReceivedRequestsRvAdapter
import com.example.chatapp.Models.FriendRequest
import com.example.chatapp.Models.User
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.Utilities.Util
import com.example.chatapp.ViewModels.FriendViewModel
import com.example.chatapp.databinding.FragmentReceivedRequestsBinding
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReceivedRequests : Fragment() {
    private val friendViewModel:FriendViewModel by activityViewModels()
    private lateinit var currentUser:FirebaseUser
    private lateinit var binding: FragmentReceivedRequestsBinding
    private lateinit var receivedRequestsRv:RecyclerView
    private val receivedRequests = mutableListOf<FriendRequest>()
    private val userDetails = mutableMapOf<String, User>()
    private lateinit var receivedRequestsRvAdapter: ReceivedRequestsRvAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReceivedRequestsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser = FirebaseService.firebaseAuth.currentUser!!

        receivedRequestsRv = binding.ReceivedRequestsRecyclerView
        receivedRequestsRv.layoutManager = LinearLayoutManager(requireContext())
        receivedRequestsRvAdapter =
            ReceivedRequestsRvAdapter(requireContext(), receivedRequests, userDetails,
                { request ->
                    friendViewModel.respondToFriendRequests(currentUser.uid,request.requestId, true)

                },
                { request ->
                    friendViewModel.respondToFriendRequests(currentUser.uid,request.requestId, false)

                },
                {
                    user->
                    Util.friendsViewPageState = 2
                    Util.loadOtherUserProfile(user.id,parentFragmentManager,true)
                }
            )

        receivedRequestsRv.adapter = receivedRequestsRvAdapter

        loadReceivedRequests()

    }

    private fun loadReceivedRequests() {
        val currentUserId = currentUser.uid
        friendViewModel.loadReceivedRequests(currentUserId)
        friendViewModel.receivedRequests.observe(viewLifecycleOwner) { requests ->
            receivedRequests.clear()
            receivedRequests.addAll(requests)
            binding.receivedrequestscount.text = "Received requests : ${receivedRequests.size}"
            Toast.makeText(requireContext(),"received requests : ${receivedRequests.size}", Toast.LENGTH_SHORT).show()

            val senderIds = requests.map { it.fromUserId }

            friendViewModel.loadUserDetails(senderIds)


            friendViewModel.userDetails.observe(viewLifecycleOwner) { users ->
                userDetails.clear()
                userDetails.putAll(users)


                receivedRequestsRvAdapter.notifyDataSetChanged()
            }
        }
    }

}