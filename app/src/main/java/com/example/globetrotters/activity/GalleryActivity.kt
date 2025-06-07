package com.example.globetrotters.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.globetrotters.R
import com.example.globetrotters.adapters.GalleryAdapter
import com.example.globetrotters.database.PhotoEntity
import com.example.globetrotters.viewmodel.PhotoViewModel

class GalleryActivity : AppCompatActivity() {

    private lateinit var photoViewModel: PhotoViewModel
    private var travelId: Int = 0
    private lateinit var galleryAdapter: GalleryAdapter
    private var deleteModeActive = false
    private val selectedPhotoIndices = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        photoViewModel = ViewModelProvider(this)
            .get(PhotoViewModel::class.java)

        travelId = intent.getIntExtra("travel_id", 0)
        findViewById<TextView>(R.id.galleryTitleTextView).text =
            intent.getStringExtra("travel_title").orEmpty()

        val galleryRecycler = findViewById<RecyclerView>(R.id.galleryRecyclerView)
        galleryRecycler.layoutManager = GridLayoutManager(this, 3)

        galleryAdapter = GalleryAdapter(emptyList()) { position ->
            val uris = galleryAdapter.getPhotoUris()
            startActivity(Intent(this, FullScreenPhotoActivity::class.java).apply {
                putExtra("photo_uris", uris)
                putExtra("start_index", position)
            })
        }.also {
            it.onSingleDelete = { photo ->

                photoViewModel.deletePhoto(photo)
            }
        }
        galleryRecycler.adapter = galleryAdapter

        val deleteIcon = findViewById<ImageView>(R.id.galleryDeleteIcon)
        val deleteMsg = findViewById<TextView>(R.id.galleryDeleteModeMessage)

        deleteIcon.setOnClickListener {
            if (!deleteModeActive) {
                deleteModeActive = true
                selectedPhotoIndices.clear()
                deleteMsg.visibility = View.VISIBLE
                galleryAdapter.setSelectionMode(true) { idx, sel ->
                    if (sel) selectedPhotoIndices.add(idx) else selectedPhotoIndices.remove(idx)
                }
            } else {
                if (selectedPhotoIndices.isNotEmpty()) {
                    AlertDialog.Builder(this)
                        .setTitle("Conferma eliminazione")
                        .setMessage("Vuoi cancellare queste ${selectedPhotoIndices.size} foto?")
                        .setPositiveButton("Si") { _, _ ->

                            galleryAdapter.getSelectedItems().forEach { photo ->
                                photoViewModel.deletePhoto(photo)
                            }
                            exitDeleteMode(deleteMsg)
                        }
                        .setNegativeButton("No") { _, _ ->
                            exitDeleteMode(deleteMsg)
                        }
                        .show()
                } else {
                    Toast.makeText(this, "Nessuna foto selezionata", Toast.LENGTH_SHORT).show()
                    exitDeleteMode(deleteMsg)
                }
            }
        }

        photoViewModel.getPhotosForTravel(travelId).observe(this) { photos: List<PhotoEntity> ->
            galleryAdapter.updateList(photos)
        }
    }

    private fun exitDeleteMode(msg: TextView) {
        deleteModeActive = false
        selectedPhotoIndices.clear()
        msg.visibility = View.GONE
        galleryAdapter.setSelectionMode(false, null)
    }
}
