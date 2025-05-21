package com.example.globetrotters

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.globetrotters.adapters.GalleryAdapter
import com.example.globetrotters.database.TravelDatabase
import kotlinx.coroutines.launch

class GalleryActivity : AppCompatActivity() {

    private lateinit var db: TravelDatabase
    private var travelId: Int = 0
    private lateinit var galleryAdapter: GalleryAdapter

    private var deleteModeActive = false
    private val selectedPhotoIndices = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        db = TravelDatabase.getDatabase(this)
        travelId = intent.getIntExtra("travel_id", 0)
        val travelTitle = intent.getStringExtra("travel_title").orEmpty()
        findViewById<TextView>(R.id.galleryTitleTextView).text = travelTitle

        // RecyclerView in grid 3 col.
        val galleryRecycler = findViewById<RecyclerView>(R.id.galleryRecyclerView)
        galleryRecycler.layoutManager = GridLayoutManager(this, 3)

        // Adapter
        galleryAdapter = GalleryAdapter(emptyList()) { position ->
            val uris = galleryAdapter.getPhotoUris()
            val intent = Intent(this, FullScreenPhotoActivity::class.java).apply {
                putExtra("photo_uris", uris)
                putExtra("start_index", position)
            }
            startActivity(intent)
        }.apply {
            onSingleDelete = { photo ->
                lifecycleScope.launch {
                    db.photoDao().deletePhoto(photo)
                }
            }
        }
        galleryRecycler.adapter = galleryAdapter

        // Delete-mode icon & message
        val deleteIcon = findViewById<ImageView>(R.id.galleryDeleteIcon)
        val deleteMsg = findViewById<TextView>(R.id.galleryDeleteModeMessage)

        deleteIcon.setOnClickListener {
            if (!deleteModeActive) {
                deleteModeActive = true
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
                            lifecycleScope.launch {
                                galleryAdapter.getSelectedItems()
                                    .forEach { db.photoDao().deletePhoto(it) }
                            }
                            deleteMsg.visibility = View.GONE
                            galleryAdapter.setSelectionMode(false, null)
                            deleteModeActive = false
                            selectedPhotoIndices.clear()
                        }
                        .setNegativeButton("No") { _, _ ->
                            deleteMsg.visibility = View.GONE
                            galleryAdapter.setSelectionMode(false, null)
                            deleteModeActive = false
                            selectedPhotoIndices.clear()
                        }
                        .show()
                } else {
                    Toast.makeText(this, "Nessuna foto selezionata", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe LiveData
        db.photoDao().getPhotosForTravel(travelId).observe(this) {
            galleryAdapter.updateList(it)
            }
        }
}