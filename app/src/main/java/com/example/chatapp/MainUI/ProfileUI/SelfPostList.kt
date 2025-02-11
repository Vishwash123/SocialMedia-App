package com.example.chatapp.MainUI.ProfileUI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.Models.Post
import com.example.chatapp.R
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.Utilities.Util
import com.example.chatapp.ViewModels.PostViewModel
import com.example.chatapp.databinding.FragmentSelfPostListBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SelfPostList : Fragment() {
    private lateinit var binding:FragmentSelfPostListBinding
    private lateinit var postsRvAdapter: PostFeedAdapter
    private val list = mutableListOf<Post>()
    private val postViewModel:PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSelfPostListBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val position = arguments?.getInt("position")
        binding.selfPostRv.layoutManager = LinearLayoutManager(requireContext())
        val loadedPosts = postViewModel.userPostList.value
        loadedPosts?.forEach{
            list.add(it)
        }

        list.sortByDescending { it.timestamp }

        postsRvAdapter = PostFeedAdapter(requireContext(),list,parentFragmentManager,lifecycle,
            onLikePressed = {post,like->
                //postViewModel.addLike(post.posterId,post.postId,like)
                //postViewModel.addLike(post.posterId,post.postId,like)
                postViewModel.addPendingLikeUpdate(post.postId,like)

            },
            onCommentPressed = {id->
                Util.loadCommentSection(parentFragmentManager,true,id)
            },
            onDoubleTap = {

            })
        binding.selfPostRv.adapter = postsRvAdapter
        binding.selfPostRv.scrollToPosition(position!!)
        postViewModel.userPostList.observe(viewLifecycleOwner){postlist->
            list.clear()
            list.addAll(postlist!!)
            list.sortByDescending { it.timestamp }
            postsRvAdapter.notifyDataSetChanged()
        }

        binding.selfPostListBackButton.setOnClickListener{
            parentFragmentManager.popBackStack()
        }


    }

    override fun onPause() {
        postViewModel.completePendingUpdates(FirebaseService.firebaseAuth.currentUser!!.uid)

        super.onPause()

    }

    override fun onDestroy() {
        postViewModel.completePendingUpdates(FirebaseService.firebaseAuth.currentUser!!.uid)

        super.onDestroy()
    }
}