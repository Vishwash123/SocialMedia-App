package com.example.chatapp.MainUI.FeedUI

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.example.chatapp.R
import com.example.chatapp.Utilities.Util
import com.example.chatapp.databinding.FragmentFeedBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class Feed : Fragment() {

    private lateinit var binding:FragmentFeedBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFeedBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

         val mediaLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()){uris->
            uris?.let{mediaUris->
                if(mediaUris.isNotEmpty()){
                    openNewPostFragment(ArrayList(mediaUris))
                }
            }

         }


        binding.newPostButton.addOnMenuItemClickListener{fab,textView,itemId->
            when(itemId){
                R.id.newPostFabButton->{
                    mediaLauncher.launch("*/*")
                }
                R.id.newStoryFabButton->{

                }
            }
        }

    }

    private fun openNewPostFragment(arrayList: ArrayList<Uri>) {
        val fragment = NewPost()
        val bundle = Bundle()
        bundle.putParcelableArrayList("uris",arrayList)
        fragment.arguments = bundle
        Util.loadFragment(parentFragmentManager,fragment,true)
    }


}