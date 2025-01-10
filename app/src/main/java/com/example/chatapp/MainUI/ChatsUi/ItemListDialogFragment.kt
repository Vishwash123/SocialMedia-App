package com.example.chatapp.MainUI.ChatsUi

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.chatapp.Models.BottomSheetItem
import com.example.chatapp.R
import com.example.chatapp.Utilities.AttachmentUtils
import com.example.chatapp.Utilities.AttachmentUtils.getFileTypeFromUri
import com.example.chatapp.Utilities.AuthUtils
import com.example.chatapp.Utilities.FirebaseService
import com.example.chatapp.databinding.FragmentItemListDialogListDialogItemBinding
import com.example.chatapp.databinding.FragmentItemListDialogListDialogBinding

// TODO: Customize parameter argument names

/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    ItemListDialogFragment.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 */



class ItemListDialogFragment : BottomSheetDialogFragment() {
    private lateinit var  ARG_OTHER_USER_ID:String
    private var _binding: FragmentItemListDialogListDialogBinding? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var audioPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var documentPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var photoLauncher:ActivityResultLauncher<String>



    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

         _binding = FragmentItemListDialogListDialogBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARG_OTHER_USER_ID = arguments?.getString("otherUserId")!!
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        ARG_OTHER_USER_ID = arguments?.getString("otherUserid")!!


//Log.d("bottom sheet other id","$ARG_OTHER_USER_ID")
        val itemList = listOf(
            BottomSheetItem("Camera",R.drawable.camera),
            BottomSheetItem("Image",R.drawable.image),
            BottomSheetItem("Video",R.drawable.video),
            BottomSheetItem("Audio",R.drawable.audio),
            BottomSheetItem("Document",R.drawable.document),
            BottomSheetItem("User",R.drawable.user)
        )

        binding.list.layoutManager = GridLayoutManager(context, 3)
        binding.list.adapter = ItemAdapter(6,itemList)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if(result.resultCode==Activity.RESULT_OK){
                val uri = result.data?.data

                val previewFragment = Preview();
                Log.d("uricheck","$uri")
                Toast.makeText(requireContext(),"$uri",Toast.LENGTH_LONG).show()
                uri?.let{
                    val bundle = Bundle()
                    bundle.putParcelable("uri",it);
                    bundle.putString("type","image")
                    bundle.putString("otherUserid", ARG_OTHER_USER_ID)
                    previewFragment.arguments = bundle
                    parentFragmentManager.beginTransaction().replace(R.id.mainuiFragContainer,previewFragment).commit()

                }
                 }
        }

        videoPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if(result.resultCode==Activity.RESULT_OK){
                val uri = result.data?.data
                requireContext().contentResolver.takePersistableUriPermission(uri!!,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                val previewFragment = Preview();

                uri?.let{
                    val bundle = Bundle()
                    bundle.putParcelable("uri",it);
                    bundle.putString("type","video")
                    bundle.putString("otherUserid", ARG_OTHER_USER_ID)
                    previewFragment.arguments = bundle
                }



                parentFragmentManager.beginTransaction().replace(R.id.mainuiFragContainer,previewFragment).commit()
            }
        }

        audioPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if(result.resultCode==Activity.RESULT_OK){
                val uri = result.data?.data
                val previewFragment = Preview();

                uri?.let{
                    val bundle = Bundle()
                    bundle.putParcelable("uri",it);
                    bundle.putString("otherUserid", ARG_OTHER_USER_ID)
                    bundle.putString("type","audio")
                    previewFragment.arguments = bundle
                }



                parentFragmentManager.beginTransaction().replace(R.id.mainuiFragContainer,previewFragment).commit()
            }
        }

        documentPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if(result.resultCode==Activity.RESULT_OK){
                val uri = result.data?.data
                val previewFragment = Preview();

                uri?.let{
                    val bundle = Bundle()
                    bundle.putParcelable("uri",it);
                    bundle.putString("type","document")
                    bundle.putString("otherUserid", ARG_OTHER_USER_ID)
                    previewFragment.arguments = bundle
                }



                parentFragmentManager.beginTransaction().replace(R.id.mainuiFragContainer,previewFragment).commit()
            }
        }

         photoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()){uri->
            uri?.let {
                val photoUri = it;
                val path = AuthUtils.getRealPathFromURI(requireContext(),photoUri)
                loadPreviewFragment(photoUri,path, "image")
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if(result.resultCode==Activity.RESULT_OK){
                val uri = result.data?.data
                val path = AuthUtils.getRealPathFromURI(requireContext(),uri!!)
                val type = AttachmentUtils.getFileTypeFromUri(uri,path,requireContext())
                val previewFragment = Preview();

                uri?.let{
                    val bundle = Bundle()
                    bundle.putParcelable("uri",it);

                    bundle.putString("type",type)
                    bundle.putString("otherUserid", ARG_OTHER_USER_ID)
                    previewFragment.arguments = bundle
                }



                parentFragmentManager.beginTransaction().replace(R.id.mainuiFragContainer,previewFragment).commit()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun onItemClicked(item:BottomSheetItem){
     //   requestPermissions()
        when(item.text){
            "Camera"->openCameraDialog()
            "Image"->{
               pickFile("image")
            }
            "Video"->AttachmentUtils.pickFile(requireContext(),"video",videoPickerLauncher)
            "Document"->AttachmentUtils.documentPicker(documentPickerLauncher)
            "Audio"->AttachmentUtils.audioPicker(audioPickerLauncher)
            else->return
        }
    }

    private fun openCameraDialog() {
        val inflater = LayoutInflater.from(requireContext())
        val view:View = inflater.inflate(R.layout.camera_picker_dialog,null)
        val imageButton:ImageView = view.findViewById(R.id.cameraPickerImage)
        val videoButton:ImageView = view.findViewById(R.id.cameraPickerVideo)

        val dialog = AlertDialog.Builder(context).setView(view).create()

        imageButton.setOnClickListener{
            val imageUri = AttachmentUtils.createMediaFile(requireContext(),"image","${FirebaseService.firebaseAuth.currentUser!!.uid}_${System.currentTimeMillis()}")
            imageUri?.let { AttachmentUtils.captureMedia("image",cameraLauncher,imageUri) }
            dialog.dismiss()
        }

        videoButton.setOnClickListener{
            val videoUri = AttachmentUtils.createMediaFile(requireContext(),"video","${FirebaseService.firebaseAuth.currentUser!!.uid}_${System.currentTimeMillis()}")
            videoUri?.let { AttachmentUtils.captureMedia("video",cameraLauncher,videoUri) }
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounder_corners)

        dialog.show()
    }

    private inner class ViewHolder internal constructor(binding: FragmentItemListDialogListDialogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val image:ImageView = binding.itemPhoto
         val text:TextView = binding.bottomSheetItemText
    }

    private inner class ItemAdapter internal constructor(private val mItemCount: Int,private val itemlist:List<BottomSheetItem>) :
        RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            return ViewHolder(
                FragmentItemListDialogListDialogItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )

        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.text.text = itemlist[position].text

            holder.image.setBackgroundResource(itemlist[position].photoId)

            holder.itemView.setOnClickListener{
                onItemClicked(itemlist[position])
            }





        }

        override fun getItemCount(): Int {
            return itemlist.size
        }
    }

    companion object {


        fun newInstance(otherUserId:String,context: Context): ItemListDialogFragment {
            val fragment = ItemListDialogFragment()
            val bundle = Bundle()
            bundle.putString("otherUserId",otherUserId)
            fragment.arguments = bundle

            return fragment
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("resultact","done")
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==1 && resultCode == Activity.RESULT_OK && data!=null && data.data!=null){
            val uri = data.data
            Log.d("checking uri","$uri")
            val path = AuthUtils.getRealPathFromURI(requireContext(),uri!!)
            val type = getFileTypeFromUri(uri,path,requireContext())
            loadPreviewFragment(uri,path,type)
            dismiss()
        }
        else{
            Log.d("failed file","$requestCode")
        }
    }

    private fun loadPreviewFragment(uri: Uri, path: String?, type: String) {
        val previewFrag = Preview()
        val bundle = Bundle()
        bundle.putString("otherUserid", ARG_OTHER_USER_ID)
        bundle.putString("type",type)
        bundle.putParcelable("uri",uri)
        previewFrag.arguments = bundle

        parentFragmentManager.beginTransaction().replace(R.id.mainuiFragContainer,previewFrag).addToBackStack(null).commit()
        dismiss()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == 1) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted, proceed with file picking
//                AttachmentUtils.pickFile(requireContext(), "image", imagePickerLauncher)
//            } else {
//                // Permission denied, handle accordingly (e.g., disable functionality)
//                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//




    fun pickFile(fileType:String){
        val intent = Intent()
        when(fileType){
            "image"->{
                Log.d("triggered","yo pick file")
//                intent.type = "image/*"
//                intent.action = Intent.ACTION_GET_CONTENT
//                //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
//                startActivityForResult(Intent.createChooser(intent,"Select an image"),1)
                photoLauncher.launch("image/*")
            }
            "video"->{

            }
            "audio"->{

            }
            "document"->{

            }
            "camera"->{

            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("bslog","paused")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("bslog","destroyed")
    }

    





}