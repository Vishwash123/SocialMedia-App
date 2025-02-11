package com.example.chatapp.Utilities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.chatapp.MainUI.ChatsUi.ItemListDialogFragment
import com.example.chatapp.Models.PostType
import com.example.chatapp.MyApplication

import com.example.chatapp.Utilities.AuthUtils.getRealPathFromURI
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object AttachmentUtils {

    fun getFileTypeFromUri(uri: Uri, filePath:String?,context: Context): String {
        // Get the file path from the URI

        // If file path is null, return a default type (could be an error case)
        if (filePath.isNullOrEmpty()) {
            return "unknown"
        }

        // Extract the file extension (e.g., "jpg", "mp4", etc.)
        val file = File(filePath)
        val fileExtension = file.extension.toLowerCase()

        // Determine the file type based on the file extension
        return when (fileExtension) {
            "jpg", "jpeg", "png", "gif", "bmp", "webp" -> "image"
            "mp4", "avi", "mov", "mkv", "flv" -> "video"
            "mp3", "wav", "aac", "flac" -> "audio"
            "pdf" -> "document"
            else -> "unknown" // Handle other types if needed
        }
    }

//    fun pickFile(context: Context,fileType:String,fileLauncher:ActivityResultLauncher<Intent>){
////        if (fileType == "image" || fileType == "video" || fileType == "audio") {
////            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
////                // Request permission if not granted
////                ActivityCompat.requestPermissions(context as Activity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
////                return
////            }
////        }
//
//        val intent = Intent(Intent.ACTION_PICK).apply {
//            type = "$fileType/*"
//            when(fileType){
//                "image"-> this.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//                "video"-> this.data = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//                "audio"-> this.data = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//                else-> throw IllegalArgumentException("Unsupported File Type")
//
//            }
//        }
//        fileLauncher.launch(intent)
//    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun pickFile(context: Context, fileType: String, fileLauncher: ActivityResultLauncher<Intent>) {


//            val intent = Intent()
//            intent.type = "image/*"
//            intent.action = Intent.ACTION_GET_CONTENT
//            startActivityForResult(Intent.createChooser(intent, "Select an Image"), 1)



//        val intent = Intent(Intent.ACTION_PICK).apply {
//            type = "$fileType/*"
//            when (fileType) {
//                "image" -> data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//                "video" -> data = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//                "audio" -> data = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//                else -> throw IllegalArgumentException("Unsupported File Type")
//            }
//        }
//        fileLauncher.launch(intent)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
     fun shouldRequestMediaPermission(context: Context, fileType: String): Boolean {
        val permission = when (fileType) {
            "image" -> android.Manifest.permission.READ_MEDIA_IMAGES
            "video" -> android.Manifest.permission.READ_MEDIA_VIDEO
            "audio" -> android.Manifest.permission.READ_MEDIA_AUDIO
            else -> throw IllegalArgumentException("Unsupported File Type")
        }
        return ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
     fun requestMediaPermission(context: Context, fileType: String, fileLauncher: ActivityResultLauncher<Intent>) {
        val permission = when (fileType) {
            "image" -> android.Manifest.permission.READ_MEDIA_IMAGES
            "video" -> android.Manifest.permission.READ_MEDIA_VIDEO
            "audio" -> android.Manifest.permission.READ_MEDIA_AUDIO
            else -> throw IllegalArgumentException("Unsupported File Type")
        }


        ActivityCompat.requestPermissions(context as Activity, arrayOf(permission), 1)
    }


    fun createMediaFile(context: Context,type:String,prefix:String=""):Uri?{
        val mediaDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = when(type){
            "image" -> "IMG_${prefix}_${timeStamp}.jpg"
            "video" -> "VID_${prefix}_${timeStamp}.mp4"
            else->null
        }


        val file = fileName?.let {
            File(mediaDir,it)
        }

        return file?.let { Uri.fromFile(it) }

    }


    fun captureMedia(mediaType:String,cameraLauncher:ActivityResultLauncher<Intent>,outputUri: Uri?){
        val intent = Intent().apply {
            when(mediaType){
                "image"-> this.action = MediaStore.ACTION_IMAGE_CAPTURE
                "video"-> this.action = MediaStore.ACTION_VIDEO_CAPTURE
                else-> throw IllegalArgumentException("Unsupported File Type")
            }


            outputUri?.let{
                putExtra(MediaStore.EXTRA_OUTPUT,it)
            }
        }
        cameraLauncher.launch(intent)
    }

    fun audioPicker(audioLauncher:ActivityResultLauncher<Intent>){
        val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
        audioLauncher.launch(intent)
    }


    fun documentPicker(documentLauncher:ActivityResultLauncher<Intent>){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        documentLauncher.launch(intent)
    }


    fun getPostType(fileUris: List<Uri>, context: Context): PostType {
        var hasImages = false
        var hasVideos = false

        for (fileUri in fileUris) {
            val mimeType = getMimeType(context, fileUri)

            if (mimeType?.startsWith("image") == true) {
                hasImages = true
            } else if (mimeType?.startsWith("video") == true) {
                hasVideos = true
            }

            // If both types are found, it's a combined post
            if (hasImages && hasVideos) {
                return PostType.COMBINED
            }
        }

        return when {
            hasImages -> PostType.PHOTO
            hasVideos -> PostType.VIDEO
            else->PostType.UNKNOWN
        }
    }


    fun getMimeType(context: Context, uri: Uri): String? {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension?.lowercase())
        }
    }

    fun getFileTypeFromUrl(url: String): String {
        return when {
            url.endsWith(".jpg", true) || url.endsWith(".jpeg", true) || url.endsWith(".png", true) || url.endsWith(".gif", true) -> "photo"
            url.endsWith(".mp4", true) || url.endsWith(".mkv", true) || url.endsWith(".avi", true) -> "video"
            else -> "unknown"
        }
    }



}