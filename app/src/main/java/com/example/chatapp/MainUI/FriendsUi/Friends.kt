package com.example.chatapp.MainUI.FriendsUi

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
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
    private lateinit var viewPageCallBack:ViewPager2.OnPageChangeCallback
    lateinit var srch : ImageView
    private var isTabSelected = false
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
       binding.friendsNavContainerVP.isSaveEnabled = false
        binding.friendsNavContainerVP.setCurrentItem(Util.friendsViewPageState, false)




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
        binding.friendsNav.addOnTabSelectedListener(object:OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
//                if(tab!=null){
//                    binding.friendsNavContainerVP.currentItem = tab.position

                tab?.let {
//                    executeAfterFragmentManagerIdle {
//
//                        if (binding.friendsNavContainerVP.currentItem != it.position) {
//                            binding.friendsNavContainerVP.setCurrentItem(it.position, true)
//                        }
//                    }




                            binding.friendsNavContainerVP.setCurrentItem(it.position, true)


//

                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })


        binding.friendsNav.addTab(binding.friendsNav.newTab().setText("Friends"))
        binding.friendsNav.addTab(binding.friendsNav.newTab().setText("Sent"))
        binding.friendsNav.addTab(binding.friendsNav.newTab().setText("Received"))



        binding.friendsNavContainerVP.adapter = fragmentAdapter
        // Ensure ViewPager2 starts with the correct tab on fragment creation
        Handler(Looper.getMainLooper()).post {
            // Set the ViewPager2 to the stored selected tab position
            binding.friendsNavContainerVP.setCurrentItem(Util.friendsViewPageState, false) // No animation
        }



        viewPageCallBack = object :ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (binding.friendsNav.selectedTabPosition != position) {
                    binding.friendsNav.getTabAt(position)?.select()
                }
//                binding.friendsNav.selectTab(binding.friendsNav.getTabAt(position))
            }
        }

        binding.friendsNavContainerVP.registerOnPageChangeCallback(viewPageCallBack)





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
            lifecycleScope.launchWhenResumed {
                if(parentFragmentManager.findFragmentById(R.id.friendFragCont) !is FriendSearchResults){
                parentFragmentManager.beginTransaction()
                    .replace(R.id.friendFragCont, FriendSearchResults())
                    .addToBackStack(null)
                    .commit()
            }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.friendSearchIcon.visibility = View.VISIBLE
        binding.friendsNav.visibility =     View.VISIBLE
        binding.textView2.visibility = View.VISIBLE
        binding.friendsNavContainerVP.visibility = View.VISIBLE
        binding.friendsEntireContainer.visibility = View.VISIBLE

        binding.friendsNavContainerVP.setCurrentItem(Util.friendsViewPageState, true)



    }

    fun AppCompatActivity.replaceFragment(fragment: Fragment) {
        if (!isDestroyed && !isFinishing) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainuiFragContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        }






//    private fun executeAfterFragmentManagerIdle(action: () -> Unit) {
//        val handler = Handler(Looper.getMainLooper())
//        handler.postDelayed(object : Runnable {
//            override fun run() {
//                if (!parentFragmentManager.isStateSaved ) {
//                    action()
//                } else {
//                    handler.postDelayed(this, 50) // Check again after a short delay
//                }
//            }
//        }, 50)
//    }






}