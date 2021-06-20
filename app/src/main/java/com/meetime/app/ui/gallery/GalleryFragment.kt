package com.meetime.app.ui.gallery

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.meetime.app.R
import com.meetime.app.utils.PreviewActivity
import com.soundcloud.android.crop.Crop
import kotlinx.android.synthetic.main.fragment_gallery.*
import java.io.*


class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    private var imageUri: Uri? = null
    private var croppedImageUri: Uri? = null

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            imageUri = it.data?.data
            val selectedBitmap : Bitmap? = imageUri?.let { it1 -> getBitmapFromUri(it1) }

            val selectedImgFile = File(context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "_selectedImg.jpg")

            if (selectedBitmap != null) {
                Log.wtf("TAG", "Bitmap is not null")
                convertBitmaptoFile(selectedImgFile, selectedBitmap)
            }

            val croppedImgFile = File(context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "_croppedImg.jpg")

            Crop.of(Uri.fromFile(selectedImgFile), Uri.fromFile(croppedImgFile)).start(activity)
            imageUri = Uri.fromFile(selectedImgFile)
            croppedImageUri = Uri.fromFile(croppedImgFile)
        }

    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor: ParcelFileDescriptor =
            requireActivity().contentResolver.openFileDescriptor(uri, "r")!!
        val fileDescriptor: FileDescriptor = parcelFileDescriptor.fileDescriptor
        val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    fun convertBitmaptoFile(destinationFile: File, bitmap: Bitmap) {
        //create a file to write bitmap data
        destinationFile.createNewFile()
        //Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos)
        val bitmapData = bos.toByteArray()
        //write the bytes in file
        val fos = FileOutputStream(destinationFile)
        fos.write(bitmapData)
        fos.flush()
        fos.close()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_gallery, container, false)
        val textView: TextView = root.findViewById(R.id.text_gallery)
        galleryViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onResume() {
        super.onResume()

        if(croppedImageUri != null) {
            val bitmap : Bitmap? = MediaStore.Images.Media.getBitmap(context?.contentResolver, croppedImageUri)
            gallery_avatar.setImageBitmap(bitmap)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gallery_avatar.setOnClickListener {
            if(gallery_avatar.drawable == null) {
                val galleryIntent =
                    Intent(Intent.ACTION_GET_CONTENT)
                galleryIntent.type = "image/*"

                getContent.launch(galleryIntent)
            }
            else{
                val previewIntent = Intent(requireContext(), PreviewActivity::class.java)
                previewIntent.putExtra("fullImgUri", imageUri)
                startActivity(previewIntent)
            }
        }

    }

}