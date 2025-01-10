package com.example.chatapp.MainUI.FriendsUi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView.OnQueryTextListener
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.MainUI.FriendsUi.FriendsUiRvs.SearchResultsRvAdapter
import com.example.chatapp.Models.FriendRequest
import com.example.chatapp.Models.User
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.ViewModels.FriendViewModel
import com.example.chatapp.databinding.FragmentFriendSearchResultsBinding
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendSearchResults : Fragment() {
    private lateinit var currentUser: FirebaseUser
    private lateinit var currentSentReq:List<FriendRequest>
    private lateinit var sentReqMap:Map<String,String>
    private lateinit var friendsMap:Map<String,User>
    private lateinit var friendSeachRv:RecyclerView
    private lateinit var friendSeachRvAdapter: SearchResultsRvAdapter
    private lateinit var searchQuery:String
    private lateinit var binding:FragmentFriendSearchResultsBinding
    private val friendViewModel:FriendViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFriendSearchResultsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.friendsearchtext.visibility = View.GONE
        searchQuery = ""
        binding.friensSearchBar.setOnQueryTextListener(object :OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchQuery = query?:""
                binding.friendsearchtext.visibility =View.VISIBLE
                loadSearchResults(searchQuery)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
     //   Toast.makeText(requireContext(),"$searchQuery",Toast.LENGTH_SHORT).show()
        currentUser = FirebaseService.firebaseAuth.currentUser?:return
        friendSeachRv = binding.friendSearchRv
        friendSeachRv.layoutManager = LinearLayoutManager(requireContext())

        observeData()

//
//        friendViewModel.loadSentRequests(currentUser.uid)
//        Toast.makeText(requireContext(),"After Loading sent req",Toast.LENGTH_SHORT).show()
//        friendViewModel.sentRequests.observe(viewLifecycleOwner){requests->
//            Toast.makeText(requireContext(),"fetching sent req",Toast.LENGTH_SHORT).show()
//            currentSentReq = requests
//            Toast.makeText(requireContext(),"sent ${currentSentReq.size}",Toast.LENGTH_SHORT).show()
//            sentReqMap = currentSentReq.associate { it.toUserId to it.requestId }
//
//            if(!searchQuery.isNullOrEmpty()) {
//                Toast.makeText(requireContext(),"Loading search results",Toast.LENGTH_SHORT).show()
//                loadSearchResults(searchQuery)
//            }
//
//
//            friendViewModel.searchResults.observe(viewLifecycleOwner){users->
//                binding.friendsearchtext.text = "Search results for $searchQuery : ${users.size}"
//                friendSeachRvAdapter = SearchResultsRvAdapter(
//                    requireContext(),
//                    users,
//                    sentReqMap,
//                    {user->friendViewModel.sendFriendRequest(currentUser.uid,user.id)
//                        Toast.makeText(requireContext(),"Sent req",Toast.LENGTH_SHORT).show()
//                        friendSeachRvAdapter.notifyDataSetChanged()},
//                    {reqId->friendViewModel.cancelFriendRequest(currentUser.uid,reqId)
//                        friendSeachRvAdapter.notifyDataSetChanged()},
//                    {friend->
//                        friendViewModel.removeFriend(currentUser.uid,friend.id)
//                        friendSeachRvAdapter.notifyDataSetChanged()
//
//                    })
//
//                friendSeachRv.adapter = friendSeachRvAdapter
//            }
//
//
//        }


    }

    private fun observeData(){
        val userId = currentUser.uid
        friendViewModel.loadSentRequests(userId)
        friendViewModel.sentRequests.observe(viewLifecycleOwner){requests->
            currentSentReq = requests
            sentReqMap = currentSentReq.associate { it.toUserId to it.requestId }
            updateSearchResults()
        }

        friendViewModel.loadFriendsList(userId)
        friendViewModel.friendList.observe(viewLifecycleOwner){friends->
            friendsMap = friends.associateBy{it.id}
            updateSearchResults()
        }
    }

    private fun updateSearchResults(){
        if(searchQuery.isNotEmpty()){
            loadSearchResults(searchQuery)
        }
    }

    private fun loadSearchResults(searchQuery: String) {
        friendViewModel.searchUsers(currentUser.uid,searchQuery)
        friendViewModel.searchResults.observe(viewLifecycleOwner){users->
            binding.friendsearchtext.text = "Search results for \"$searchQuery\" : ${users.size}"
            friendSeachRvAdapter = SearchResultsRvAdapter(
                requireContext(),
                users,
                sentReqMap,
                friendsMap,
                onAddClick = {user->
                    friendViewModel.sendFriendRequest(currentUser.uid,user.id)
                    friendSeachRvAdapter.notifyDataSetChanged()
                },
                onCancelClick = {requestId->
                    friendViewModel.cancelFriendRequest(currentUser.uid,requestId)
                    friendSeachRvAdapter.notifyDataSetChanged()
                },
                onRemoveClick = {user->
                    friendViewModel.removeFriend(currentUser.uid,user.id)
                    friendSeachRvAdapter.notifyDataSetChanged()
                }

            )

            friendSeachRv.adapter = friendSeachRvAdapter
//            friendSeachRvAdapter.notifyDataSetChanged()
        }




    }


}


