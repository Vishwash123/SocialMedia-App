package com.example.chatapp.MainUI

import android.os.Binder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.replace
import androidx.viewpager2.widget.ViewPager2
import com.example.chatapp.MainActivity2
import com.example.chatapp.MainUI.FriendsUi.FriendList
import com.example.chatapp.MainUI.FriendsUi.FriendSearchResults
import com.example.chatapp.MainUI.FriendsUi.ReceivedRequests
import com.example.chatapp.MainUI.FriendsUi.SentRequests
import com.example.chatapp.MainUI.MainUiRvs.FriendsFragmentAdapter
import com.example.chatapp.R
import com.example.chatapp.Utilities.Util
import com.example.chatapp.databinding.FragmentFriendsBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Friends : Fragment() {

    private lateinit var binding:FragmentFriendsBinding
    private lateinit var fragmentAdapter: FriendsFragmentAdapter
    lateinit var srch : ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentFriendsBinding.inflate(inflater,container,false)
        srch = binding.friendSearchIcon
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.friendsNavContainer.visibility = View.VISIBLE
//        binding.friendsNav.visibility = View.VISIBLE
//        binding.textView2.visibility = View.VISIBLE
//        binding.friendSearchIcon.visibility = View.VISIBLE


//        parentFragmentManager.addOnBackStackChangedListener {
//            if(isVisible){
//                childFragmentManager.fragments.forEach{fragment->
//                    if(fragment is )
//                }
//            }
//        }

//        showNestedUI()




        fragmentAdapter = FriendsFragmentAdapter(parentFragmentManager,lifecycle)


        binding.friendsNav.addTab(binding.friendsNav.newTab().setText("Friends"))
        binding.friendsNav.addTab(binding.friendsNav.newTab().setText("Sent"))
        binding.friendsNav.addTab(binding.friendsNav.newTab().setText("Received"))


        binding.friendsNavContainerVP.adapter = fragmentAdapter

        binding.friendsNav.addOnTabSelectedListener(object:OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab!=null){
                    binding.friendsNavContainerVP.currentItem = tab.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        binding.friendsNavContainerVP.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.friendsNav.selectTab(binding.friendsNav.getTabAt(position))
            }

        })


        binding.friendSearchIcon.setOnClickListener{
            val parentFragmentView = parentFragment?.view
            binding.friendSearchIcon.visibility = View.GONE
            binding.friendsNav.visibility =     View.GONE
            binding.textView2.visibility = View.GONE
            binding.friendsNavContainerVP.visibility = View.GONE
            binding.friendsEntireContainer.visibility = View.GONE
           // Util.loadFragment(MainActivity2.suppFragManager,FriendSearchResults()
           // Util.replaceFragment(FriendSearchResults())
           // (requireActivity() as AppCompatActivity).replaceFragment(FriendSearchResults())
            parentFragmentManager.beginTransaction().hide(FriendSearchResults()).add(R.id.friendFragCont, FriendSearchResults()).addToBackStack(null).commit()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.friendSearchIcon.visibility = View.VISIBLE
        binding.friendsNav.visibility =     View.VISIBLE
        binding.textView2.visibility = View.VISIBLE
        binding.friendsNavContainerVP.visibility = View.VISIBLE
        binding.friendsEntireContainer.visibility = View.VISIBLE
    }

    fun AppCompatActivity.replaceFragment(fragment: Fragment) {
        if (!isDestroyed && !isFinishing) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainuiFragContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
    }





}