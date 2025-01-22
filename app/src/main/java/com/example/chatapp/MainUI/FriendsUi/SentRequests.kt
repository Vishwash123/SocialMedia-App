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
import com.example.chatapp.MainUI.FriendsUi.FriendsUiRvs.SentRequestsRvAdapter
import com.example.chatapp.Models.FriendRequest
import com.example.chatapp.Models.User
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.Utilities.Util
import com.example.chatapp.ViewModels.FriendViewModel
import com.example.chatapp.databinding.FragmentSentRequestsBinding
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SentRequests : Fragment() {
    private lateinit var currentUser:FirebaseUser
    private lateinit var sentReqRv:RecyclerView
    private lateinit var sentRequestsRvAdapter: SentRequestsRvAdapter
    private val sentRequests = mutableListOf<FriendRequest>()
    private val userDetails = mutableMapOf<String, User>()
    private val friendViewModel:FriendViewModel by activityViewModels()
    private lateinit var binding: FragmentSentRequestsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSentRequestsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser = FirebaseService.firebaseAuth.currentUser!!

        sentReqRv = binding.SentRequestsRecyclerView
        sentReqRv.layoutManager = LinearLayoutManager(requireContext())

        sentRequestsRvAdapter = SentRequestsRvAdapter(requireContext(), sentRequests, userDetails,
            onCancel = { request ->
                friendViewModel.cancelFriendRequest(currentUser.uid, request.requestId)
            },
            onItemClicked = {user->
                Util.friendsViewPageState=1
                Util.loadOtherUserProfile(user.id,parentFragmentManager,true)
            })
        sentReqRv.adapter = sentRequestsRvAdapter // Attach the adapter initially


        loadSentRequests()
//        friendViewModel.sentRequests.observe(viewLifecycleOwner){requests->
//            //show them in rv
//            //also handle cancelling there
//        }
    }

    private fun loadSentRequests() {
        val currentUserId = currentUser.uid
        friendViewModel.loadSentRequests(currentUserId)

        friendViewModel.sentRequests.observe(viewLifecycleOwner) { requests ->
            sentRequests.clear()
            sentRequests.addAll(requests)
            binding.sentrequestscount.text = "Sent requests : ${sentRequests.size}"
            Toast.makeText(requireContext(),"sent requests : ${sentRequests.size}",Toast.LENGTH_SHORT).show()
            val receiverIds = requests.map { it.toUserId }
            friendViewModel.loadUserDetails(receiverIds)



            friendViewModel.userDetails.observe(viewLifecycleOwner) { users ->
                userDetails.clear()
                userDetails.putAll(users)
                Toast.makeText(requireContext(),"users : ${users.size}",Toast.LENGTH_SHORT).show()


                sentRequestsRvAdapter.notifyDataSetChanged()
            }
        }
    }


}