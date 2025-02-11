package com.example.chatapp.MainUI.ProfileUI

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.Models.Comment
import com.example.chatapp.Models.CommenterData
import com.example.chatapp.R
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.ViewModels.PostViewModel
import com.example.chatapp.databinding.FragmentCommentsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Comments : Fragment() {
    private lateinit var binding:FragmentCommentsBinding
    private var imageUri:Uri? = null
    private val postViewModel:PostViewModel by activityViewModels()
    private var isReplying:Boolean = false
    private var replyCommentId:String? = null
    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var postId:String
    private val currentUserId = FirebaseService.firebaseAuth.currentUser!!.uid
    private val commentList = mutableListOf<Comment>()
    private val repliesMap = mutableMapOf<String,Comment>()
    private val commentersDataMap = mutableMapOf<String,CommenterData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCommentsBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postId = arguments?.getString("postId")!!
        binding.postCommentButton.setOnClickListener{
            Log.d("post comment xxo","comment button pressed")
            val text = binding.commentEditText.text.toString()
            handleComment(text)
            binding.commentEditText.setText("")
        }

        binding.commentBackButton.setOnClickListener{
            parentFragmentManager.popBackStack()
        }

        commentsAdapter = CommentsAdapter(
            requireContext(),
            commentList,
            repliesMap,
            commentersDataMap,
            onLikePressed = {commentId,liked->
                postViewModel.addPendingCommentUpdates(commentId,liked,null)
            },
            onReplyPressed = {commentId->
//                postViewModel.addPendingCommentUpdates(commentId,null,)
                isReplying = true
                replyCommentId = commentId
            }
        )

        binding.postCommentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.postCommentRecyclerView.adapter = commentsAdapter

         viewLifecycleOwner.lifecycleScope.launch {
             repeatOnLifecycle(Lifecycle.State.STARTED) {
                 postViewModel.commentersList.collect { commenters ->
                     Log.d("comments collection xxo", "commenters ${commenters.size}")
                     commentersDataMap.clear()
                     commentersDataMap.putAll(commenters)
                     withContext(Dispatchers.Main) {
                         commentsAdapter.notifyDataSetChanged()
                     }
                 }
             }
         }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                postViewModel.repliesList.collect { map ->
                    Log.d("comments collection xxo", "replies ${map.size}")

                    repliesMap.clear()
                    repliesMap.putAll(map)
                    withContext(Dispatchers.Main) {
                        commentsAdapter.notifyDataSetChanged()
                    }
                }


            }
        }
        postViewModel.fetchReplies(postId)


        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                postViewModel.commentsList.collect { comments ->
                    Log.d("comments collection xxo", "comments ${comments.size}")

                    commentList.clear()
                    commentList.addAll(comments)
                    withContext(Dispatchers.Main) {
                        commentsAdapter.notifyDataSetChanged()
                    }

                }





            }

        }

        postViewModel.fetchComments(postId!!)







    }

    private fun handleComment(text: String) {
        //chekc for reply and image uri accordingly post comment or reply t x
        Log.d("handling comment xxo","in")
        if(isReplying){
            Log.d("handling posting comment xxo","replying")
            val replyId = "${replyCommentId}_${currentUserId}_reply_${System.currentTimeMillis()}"
            val reply = Comment(
                replyId,
                currentUserId,
                postId,
                text = text,
                imageUri = imageUri
            )
            postViewModel.addPendingCommentUpdates(replyCommentId!!,null,reply)
            isReplying = false
            replyCommentId=null
            commentsAdapter.notifyDataSetChanged()
        }
        else{
            Log.d("handling posting comment xxo","commenting")
            val commentId = "${postId}_${currentUserId}_comment_${System.currentTimeMillis()}"
            postViewModel.addPendingComment(requireContext(),currentUserId,postId,imageUri,text,commentId)
            commentList.add(Comment())
            commentsAdapter.notifyDataSetChanged()
            imageUri=null
        }
    }


    override fun onPause() {
        postViewModel.completePendingComment(postId,requireContext(),currentUserId)
        postViewModel.completePendingCommentUpdates(postId,requireContext(),currentUserId)
        super.onPause()

    }

    override fun onDestroy() {
        postViewModel.completePendingComment(postId,requireContext(),currentUserId)
        postViewModel.completePendingCommentUpdates(postId,requireContext(),currentUserId)
        super.onDestroy()
    }

}