package com.example.chatapp.Models

import androidx.core.app.NotificationCompat.CallStyle.CallType
import com.google.firebase.Timestamp

data class Call (
    val callId:String="",
    val caller:String="",
    val receiver:String="",
    val timestamp:Long = 0L,
    val duration:Long = 0L,
    val type:Calltype=Calltype.AUDIO,
    val status:CallStatus=CallStatus.ACTIVE
)

enum class Calltype{
    AUDIO,VIDEO
}

enum class CallStatus{
    MISSED,ACCEPTED,REJECTED,ACTIVE
}


