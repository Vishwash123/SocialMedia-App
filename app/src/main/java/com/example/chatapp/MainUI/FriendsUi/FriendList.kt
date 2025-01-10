package com.example.chatapp.MainUI.FriendsUi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.MainUI.FriendsUi.FriendsUiRvs.FriendListRvAdapter
import com.example.chatapp.Models.User
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.ViewModels.FriendViewModel
import com.example.chatapp.databinding.FragmentFriendListBinding
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendList : Fragment() {
    private val friendViewModel:FriendViewModel by viewModels()
    private lateinit var binding: FragmentFriendListBinding
    private lateinit var friendListRv:RecyclerView
    private lateinit var friendListRvAdapter: FriendListRvAdapter
    private lateinit var currentUser:FirebaseUser
    private var friends = mutableListOf<User>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        // Inflate the layout for this fragment
        binding = FragmentFriendListBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUser = FirebaseService.firebaseAuth.currentUser!!
        friendViewModel.loadFriendsList(currentUser.uid)
        friendListRv = binding.friendListRecyclerView
        friendListRv.layoutManager = LinearLayoutManager(requireContext())
        friendListRvAdapter = FriendListRvAdapter(requireContext(),friends){user->
            friendViewModel.removeFriend(currentUser.uid,user.id)
        }

        val dividerItemDecoration = DividerItemDecoration(
            friendListRv.context,
            (friendListRv.layoutManager as LinearLayoutManager).orientation
        )
        friendListRv.addItemDecoration(dividerItemDecoration)
        friendListRv.adapter = friendListRvAdapter

        friendViewModel.friendList.observe(viewLifecycleOwner){Friends->
            friends.clear()
            friends.addAll(Friends)
            binding.friendcount.text = "Friends : ${friends.size}"
            Toast.makeText(requireContext(),"Friends : ${friends.size}",Toast.LENGTH_SHORT).show()
            friendListRvAdapter.notifyDataSetChanged()
        }


    }






}