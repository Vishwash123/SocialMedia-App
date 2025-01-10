package com.example.chatapp.Utilities

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

object AuthUtils {

//    /**
//     * Get the real path from a URI for images, videos, audio, or documents.
//     */
//    fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            getPathForApi29AndAbove(context, contentUri)
//        } else {
//            getPathForApiBelow29(context, contentUri)
//        }
//    }
//
//    /**
//     * For Android API 29 and above, handle document URIs.
//     */
//    private fun getPathForApi29AndAbove(context: Context, uri: Uri): String? {
//        if (DocumentsContract.isDocumentUri(context, uri)) {
//            val docId = DocumentsContract.getDocumentId(uri)
//            val split = docId.split(":")
//            val type = split[0]
//
//            return when (type) {
//                "image" -> getPathFromMediaStore(context, uri, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//                "video" -> getPathFromMediaStore(context, uri, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
//                "audio" -> getPathFromMediaStore(context, uri, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
//                "document" -> getPathFromDocument(context, uri)
//                else -> null
//            }
//        }
//        return null
//    }
//
//    /**
//     * Get the real path for images, videos, and audio from MediaStore.
//     */
//    private fun getPathFromMediaStore(context: Context, uri: Uri, mediaContentUri: Uri): String? {
//        val docId = DocumentsContract.getDocumentId(uri).split(":")[1]
//        val selection = "_id=?"
//        val selectionArgs = arrayOf(docId)
//
//        val cursor = context.contentResolver.query(mediaContentUri, arrayOf(MediaStore.MediaColumns.DATA), selection, selectionArgs, null)
//        cursor?.use {
//            if (it.moveToFirst()) {
//                val columnIndex = it.getColumnIndex(MediaStore.MediaColumns.DATA)
//                if (columnIndex != -1) {
//                    return it.getString(columnIndex)
//                }
//            }
//        }
//        return null
//    }
//
//    /**
//     * Get the real path for documents.
//     */
//    private fun getPathFromDocument(context: Context, uri: Uri): String? {
//        val cursor = context.contentResolver.query(uri, null, null, null, null)
//        cursor?.use {
//            if (it.moveToFirst()) {
//                val columnIndex = it.getColumnIndex(MediaStore.MediaColumns.DATA)
//                if (columnIndex != -1) {
//                    return it.getString(columnIndex)
//                }
//            }
//        }
//        return null
//    }
//
//    /**
//     * For Android API below 29, handle URIs for images, videos, and audio.
//     */
//    private fun getPathForApiBelow29(context: Context, uri: Uri): String? {
//        val projection = arrayOf(MediaStore.MediaColumns.DATA)
//        val cursor = context.contentResolver.query(uri, projection, null, null, null)
//        cursor?.use {
//            if (it.moveToFirst()) {
//                val columnIndex = it.getColumnIndex(MediaStore.MediaColumns.DATA)
//                if (columnIndex != -1) {
//                    return it.getString(columnIndex)
//                }
//            }
//        }
//        return null
//    }
//
//    /**
//     * Fallback method to get the display name of the file if the real path is not available.
//     */
//    fun getFileName(context: Context, uri: Uri): String? {
//        val cursor = context.contentResolver.query(uri, null, null, null, null)
//        cursor?.use {
//            if (it.moveToFirst()) {
//                val columnIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
//                if (columnIndex != -1) {
//                    return it.getString(columnIndex)
//                }
//            }
//        }
//        return null
//    }


    /**
     * Get the real path from a URI for images, videos, audio, or documents.
     */
    fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getPathForApi29AndAbove(context, contentUri)
        } else {
            getPathForApiBelow29(context, contentUri)
        }
    }

    /**
     * For Android API 29 and above, handle document URIs.
     */
    private fun getPathForApi29AndAbove(context: Context, uri: Uri): String? {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":")
            val type = split[0]

            when (type) {
                "image" -> return getPathFromMediaStore(context, uri, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                "video" -> return getPathFromMediaStore(context, uri, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                "audio" -> return getPathFromMediaStore(context, uri, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                "document", "msf" -> return getPathFromDownloads(context, uri) // Handle "Downloads"
            }
        }
        return copyFileToTemp(context,uri)
    }

    /**
     * Handle Downloads URIs.
     */
    private fun getPathFromDownloads(context: Context, uri: Uri): String? {
        val id = DocumentsContract.getDocumentId(uri)
        if (id.startsWith("raw:")) {
            return id.removePrefix("raw:") // Raw file path for older devices
        }

        try {

            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), id.toLong()
            )
            return queryColumn(context, contentUri, MediaStore.MediaColumns.DATA) // Query file path
        } catch (e: Exception) {
            e.printStackTrace()
            return copyFileToTemp(context, uri) // Fallback to copying the file
        }
    }

    /**
     * Get the file path using a content resolver query.
     */
    private fun queryColumn(context: Context, uri: Uri, column: String): String? {
        val projection = arrayOf(column)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        }
        return null
    }

    /**
     * Fallback: Copy the file to a temporary directory and return the path.
     */
    private fun copyFileToTemp(context: Context, uri: Uri): String? {
        val tempFile = File(context.cacheDir, getFileName(context, uri) ?: "temp_file")
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return tempFile.absolutePath
    }

    /**
     * Get the real path for images, videos, and audio from MediaStore.
     */
    private fun getPathFromMediaStore(context: Context, uri: Uri, mediaContentUri: Uri): String? {
        val docId = DocumentsContract.getDocumentId(uri).split(":")[1]
        val selection = "_id=?"
        val selectionArgs = arrayOf(docId)

        val cursor = context.contentResolver.query(
            mediaContentUri, arrayOf(MediaStore.MediaColumns.DATA), selection, selectionArgs, null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(MediaStore.MediaColumns.DATA)
                if (columnIndex != -1) {
                    return it.getString(columnIndex)
                }
            }
        }
        return null
    }

    /**
     * For Android API below 29, handle URIs for images, videos, and audio.
     */
    private fun getPathForApiBelow29(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                if (columnIndex != -1) {
                    return cursor.getString(columnIndex)
                }
            }
        }
        return null
    }

    /**
     * Fallback method to get the display name of the file if the real path is not available.
     */
    fun getFileName(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                if (columnIndex != -1) {
                    return it.getString(columnIndex)
                }
            }
        }
        return null
    }
}
