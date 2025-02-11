package com.example.chatapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.chatapp.MainUI.CallsUI.Calls
import com.example.chatapp.MainUI.ChatsUi.ChatRoom
import com.example.chatapp.MainUI.ChatsUi.Chats
import com.example.chatapp.MainUI.ChatsUi.NewChat
import com.example.chatapp.MainUI.ChatsUi.Preview
import com.example.chatapp.MainUI.FeedUI.Feed
import com.example.chatapp.MainUI.FriendsUi.Friends
import com.example.chatapp.MainUI.FeedUI.ImageFragment
import com.example.chatapp.MainUI.CallsUI.NewCalls
import com.example.chatapp.MainUI.FeedUI.NewPost
import com.example.chatapp.MainUI.ProfileUI.Profile
import com.example.chatapp.MainUI.ProfileUI.ProfilePicPreview
import com.example.chatapp.MainUI.FeedUI.VideoFragment
import com.example.chatapp.Models.CallStatus
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.Utilities.Util
import com.example.chatapp.Utilities.Util.loadFragment
import com.example.chatapp.Utilities.ZegoUtil
import com.example.chatapp.ViewModels.CallsViewModel
import com.example.chatapp.databinding.ActivityMain2Binding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.permissionx.guolindev.PermissionX
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarButtonName
import com.zegocloud.uikit.prebuilt.call.event.CallEndListener
import com.zegocloud.uikit.prebuilt.call.event.ZegoCallEndReason
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.internal.OutgoingCallButtonListener
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallType
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallUser
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoInvitationCallListener
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoUIKitPrebuiltCallConfigProvider
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity2 : AppCompatActivity() {
    private val callsViewModel:CallsViewModel by viewModels()
    private lateinit var binding: ActivityMain2Binding
    private lateinit var navView:BottomNavigationView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        val userId = FirebaseService.firebaseAuth.currentUser!!.uid
        val userName = FirebaseService.firebaseAuth.currentUser!!.displayName
        val callInviteConfig = ZegoUIKitPrebuiltCallInvitationConfig()
        Util.friendsViewPageState = 0
        callInviteConfig.provider =
            ZegoUIKitPrebuiltCallConfigProvider { invitationData ->
                var config: ZegoUIKitPrebuiltCallConfig? = null
                val isVideoCall = invitationData.type == ZegoInvitationType.VIDEO_CALL.value
                val isGroupCall = invitationData.invitees.size > 1
                config = if (isVideoCall && isGroupCall) {
                    ZegoUIKitPrebuiltCallConfig.groupVideoCall()
                } else if (!isVideoCall && isGroupCall) {
                    ZegoUIKitPrebuiltCallConfig.groupVoiceCall()
                } else if (!isVideoCall) {
                    ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall()
                } else {
                    ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall()
                }
                config.topMenuBarConfig.isVisible = true
                config.topMenuBarConfig.buttons.add(ZegoMenuBarButtonName.MINIMIZING_BUTTON)
                config

            }


//        callInviteConfig.callingConfig.canInvitingInCalling = true

        ZegoUIKitPrebuiltCallService.init(application,ZegoUtil.appId,ZegoUtil.appSign,userId,userName,callInviteConfig)
//        ZegoUIKitPrebuiltCallService.events.callEvents.setCallEndListener{
//
       // }

        ZegoUIKitPrebuiltCallService.events.invitationEvents.invitationListener = object :ZegoInvitationCallListener{
            override fun onIncomingCallReceived(
                callID: String?,
                caller: ZegoCallUser?,
                callType: ZegoCallType?,
                callees: MutableList<ZegoCallUser>?
            ) {

            }

            override fun onIncomingCallCanceled(callID: String?, caller: ZegoCallUser?) {

            }

            override fun onIncomingCallTimeout(callID: String?, caller: ZegoCallUser?) {

            }

            override fun onOutgoingCallAccepted(callID: String?, callee: ZegoCallUser?) {
                ZegoUtil.callStartTime = System.currentTimeMillis()
            }

            override fun onOutgoingCallRejectedCauseBusy(callID: String?, callee: ZegoCallUser?) {
                ZegoUtil.currentActiveCallId?.let { callsViewModel.updateCalls(it,0,CallStatus.MISSED) }
            }

            override fun onOutgoingCallDeclined(callID: String?, callee: ZegoCallUser?) {
               ZegoUtil.currentActiveCallId?.let { callsViewModel.updateCalls(it,0,CallStatus.REJECTED) }
             }

            override fun onOutgoingCallTimeout(
                callID: String?,
                callees: MutableList<ZegoCallUser>?
            ) {
                ZegoUtil.currentActiveCallId?.let { callsViewModel.updateCalls(it,0,CallStatus.MISSED) }

            }

        }

            ZegoUIKitPrebuiltCallService.events.invitationEvents.outgoingCallButtonListener = object :OutgoingCallButtonListener{
                override fun onOutgoingCallCancelButtonPressed() {
                    ZegoUtil.currentActiveCallId?.let { callsViewModel.updateCalls(it,0,CallStatus.MISSED) }

                }

            }


        ZegoUIKitPrebuiltCallService.events.callEvents.callEndListener = object :CallEndListener{
            override fun onCallEnd(callEndReason: ZegoCallEndReason?, jsonObject: String?) {
                if(callEndReason==ZegoCallEndReason.REMOTE_HANGUP || callEndReason==ZegoCallEndReason.LOCAL_HANGUP){
                    ZegoUtil.callEndTime = System.currentTimeMillis()
                    if(ZegoUtil.callStartTime!=null) {
                        val duration =
                            ((ZegoUtil.callEndTime!!) - (ZegoUtil.callStartTime!!)) / 1000
                        Log.d("MA2 XXO", "${ZegoUtil.currentActiveCallId}")
                        ZegoUtil.currentActiveCallId?.let {
                            callsViewModel.updateCalls(
                                it,
                                duration,
                                CallStatus.ACCEPTED
                            )
                        }
                        ZegoUtil.currentActiveCallId = null
                        ZegoUtil.callStartTime = null
                        ZegoUtil.callEndTime = null
                    }


                }
            }
//
    }



        checkAndRequestPermissions()


        binding.bottomNav.setOnItemSelectedListener { item->
            when(item.itemId){
                R.id.chats->{
                    loadFragment(supportFragmentManager, Chats(),true)
                    Util.friendsViewPageState=0
                    true
                }
                R.id.feed->{
                    loadFragment(supportFragmentManager, Feed(),true)
                    Util.friendsViewPageState=0
                    true
                }
                R.id.friend->{
                    loadFragment(supportFragmentManager, Friends(),true)
                    true
                }
                R.id.calls->{
                    loadFragment(supportFragmentManager, Calls(),true)
                    Util.friendsViewPageState=0
                    true
                }
                R.id.profile->{
                    loadFragment(supportFragmentManager, Profile(),true)
                    Util.friendsViewPageState=0
                    true
                }
                else->false

            }
        }

        supportFragmentManager.registerFragmentLifecycleCallbacks(object :FragmentManager.FragmentLifecycleCallbacks(){
            override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                super.onFragmentResumed(fm, f)
                when(f){
                    is NewChat,is ChatRoom,is NewPost,is NewCalls,is Preview,is ProfilePicPreview, is ImageFragment,is VideoFragment ->{
                        binding.bottomNav.visibility = View.GONE
                    }
                    else ->{
                        binding.bottomNav.visibility = View.VISIBLE
                    }
                }
            }
        },true)


        if(savedInstanceState==null){
            loadFragment(supportFragmentManager, Chats(),false)
        }

        

    }




    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.mainuiFragContainer)
        if (currentFragment != null && currentFragment.childFragmentManager.backStackEntryCount > 0) {
            currentFragment.childFragmentManager.popBackStack() // Navigate within the current fragment
        } else if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack() // Navigate back to the previous main fragment
        } else {
            super.onBackPressed() // Exit the app or activity
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkAndRequestPermissions() {
        // List the permissions you need
        PermissionX.init(this)
            .permissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.READ_MEDIA_AUDIO
            )
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(deniedList, "Storage Permissions Required", "These permissions are necessary to access and manage media files on your device.")
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(deniedList,"You need to grant these permissions manually in your device settings.","OK","Cancel")
                        //showForwardToSettingsDialog(deniedList, "You need to grant these permissions manually in your device settings.")
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    // All permissions are granted, proceed with your logic
                    // ...
                } else {
                    // Handle denied permissions
                    // ...
                }
            }

    }



}