package com.example.globetrotters.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.globetrotters.R
import java.io.File
import java.io.FileOutputStream

class PhotoFragment : Fragment() {

    interface OnPhotoSelectedListener {
        fun onPhotoUriSelected(uri: Uri)
    }

    private val CAMERA_REQUEST_CODE = 101
    private val IMAGE_PICK_REQUEST_CODE = 102

    private lateinit var photoPreview: ImageView
    private var currentPhotoUri: Uri? = null
    private var listener: OnPhotoSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnPhotoSelectedListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnPhotoSelectedListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_photo, container, false)

        photoPreview = view.findViewById(R.id.photoPreview)
        val takePhotoButton = view.findViewById<Button>(R.id.takePhotoButton)
        val loadPhotoButton = view.findViewById<Button>(R.id.loadPhotoButton)

        takePhotoButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val photoFile = File.createTempFile("travel_photo_", ".jpg", requireContext().cacheDir)
                currentPhotoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", photoFile)
                val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, currentPhotoUri)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            } else {
                Toast.makeText(requireContext(), "Permesso fotocamera non concesso", Toast.LENGTH_SHORT).show()
            }
        }

        loadPhotoButton.setOnClickListener {
            if (hasStoragePermission()) {
                val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
                startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
            } else {
                Toast.makeText(requireContext(), "Permesso lettura immagini non concesso", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) return

        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                currentPhotoUri?.let {
                    photoPreview.setImageURI(it)
                    listener?.onPhotoUriSelected(it)
                }
            }
            IMAGE_PICK_REQUEST_CODE -> {
                data?.data?.let { selectedImageUri ->
                    val localUri = copyImageToInternalStorage(selectedImageUri)
                    localUri?.let {
                        currentPhotoUri = it
                        photoPreview.setImageURI(it)
                        listener?.onPhotoUriSelected(it)
                    }
                }
            }
        }
    }

    private fun copyImageToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().filesDir, "travel_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
            }
        }
}