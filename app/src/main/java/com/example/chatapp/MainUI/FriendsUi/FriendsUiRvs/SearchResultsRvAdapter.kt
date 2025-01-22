package com.example.chatapp.MainUI.FriendsUi.FriendsUiRvs

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.Models.User
import com.example.chatapp.R
import de.hdodenhof.circleimageview.CircleImageView

class SearchResultsRvAdapter(
    val context: Context,
    val searchResults:List<User>,
    val sentFriendRequests:Map<String,String>,
    val currentFriends:Map<String,User>,
    val onAddClick:(User)->Unit,
    val onCancelClick:(String)->Unit,
    val onRemoveClick:(User)->Unit,
    val onItemClicked:(User)->Unit
    ):RecyclerView.Adapter<SearchResultsRvAdapter.SearchResultsViewHolder>() {
        inner class SearchResultsViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
            val profilePic = itemView.findViewById<CircleImageView>(R.id.searchResultPhoto)
            val name = itemView.findViewById<TextView>(R.id.SearchResultName)
            val bio = itemView.findViewById<TextView>(R.id.SearchResultBio)
            val actionButton = itemView.findViewById<TextView>(R.id.SearchResultAddCancel)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultsViewHolder {
        return SearchResultsViewHolder(
            LayoutInflater.from(context).inflate(R.layout.search_result_rv_item,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return searchResults.size
    }

    override fun onBindViewHolder(holder: SearchResultsViewHolder, position: Int) {
        val currentUser = searchResults[position]
        holder.name.text = currentUser.name
        holder.bio.text = currentUser.bio
        holder.itemView.setOnClickListener{
            onItemClicked(currentUser)
        }
        Log.d("search result xxo","${currentUser.bio}")
        Glide.with(context)
            .load(currentUser.profilePic)
            .placeholder(R.drawable.ic_person_placeholder)
            .into(holder.profilePic)

        when {
            currentFriends.containsKey(currentUser.id) -> {
                // User is already a friend
                holder.actionButton.text = "Remove"
                holder.actionButton.setTextColor(context.getColor(R.color.red))
                holder.actionButton.setOnClickListener {
                    onRemoveClick(currentUser) // Remove friend
                }
            }
            sentFriendRequests.containsKey(currentUser.id) -> {
                // Friend request sent
                holder.actionButton.text = "Cancel"
                holder.actionButton.setTextColor(context.getColor(R.color.red))
                holder.actionButton.setOnClickListener {
                    val requestId = sentFriendRequests[currentUser.id]
                    requestId?.let { onCancelClick(it) } // Cancel friend request
                }
            }
            else -> {
                // User is not a friend and no request sent
                holder.actionButton.text = "Add Friend"
                holder.actionButton.setTextColor(context.getColor(R.color.purple))
                holder.actionButton.setOnClickListener {
                    onAddClick(currentUser) // Send friend request
                }
            }
        }

    }
}