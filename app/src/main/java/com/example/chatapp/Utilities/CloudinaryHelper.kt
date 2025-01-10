package com.example.chatapp.Utilities

import android.content.Context
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

object CloudinaryHelper {
    private var isInit = false

        fun init(context: Context){
            if(!isInit){
                val config = mapOf(
                    "cloud_name" to "dbydr8c5p",
                    "api_key" to "576661243913843",
                    "api_secret" to "I1j72YDknkPgw2gcqdIE2JU2TSY"

                )
                MediaManager.init(context,config)
                isInit = true
            }
        }


    fun uploadFile(
        filePath:String,
        fileType:String,
        onStart:()->Unit,
        onProgress:(Int)->Unit,
        onSuccess:(String)->Unit,
        onError:(String)->Unit
    ){
        MediaManager.get()
            .upload(filePath)
            .option("resource_type",fileType)
            .callback(object:UploadCallback{
                override fun onStart(requestId: String?) {
                    onStart()
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    val progress = (bytes*100/totalBytes).toInt()
                    onProgress(progress)
                }

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val url = resultData?.get("secure_url") as String
                    onSuccess(url)

                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    onError(error!!.description)
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {

                }

            }).dispatch()
    }

}