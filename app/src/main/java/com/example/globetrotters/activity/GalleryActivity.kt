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

    // ViewModel per accedere alle foto
    private lateinit var photoViewModel: PhotoViewModel

    // ID del viaggio corrente
    private var travelId: Int = 0

    // Adapter della galleria
    private lateinit var galleryAdapter: GalleryAdapter

    // Modalità di eliminazione attiva/disattiva
    private var deleteModeActive = false

    // Indici delle foto selezionate in modalità eliminazione
    private val selectedPhotoIndices = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        // → Inizializza il ViewModel
        photoViewModel = ViewModelProvider(this)
            .get(PhotoViewModel::class.java)

        // → Recupera ID e titolo del viaggio dall'Intent
        travelId = intent.getIntExtra("travel_id", 0)
        findViewById<TextView>(R.id.galleryTitleTextView).text =
            intent.getStringExtra("travel_title").orEmpty()

        // → Imposta il layout a griglia per le foto
        val galleryRecycler = findViewById<RecyclerView>(R.id.galleryRecyclerView)
        galleryRecycler.layoutManager = GridLayoutManager(this, 3)

        // → Inizializza l'adapter con listener per la visualizzazione a schermo intero
        galleryAdapter = GalleryAdapter(emptyList()) { position ->
            val uris = galleryAdapter.getPhotoUris()
            startActivity(Intent(this, FullScreenPhotoActivity::class.java).apply {
                putExtra("photo_uris", uris)
                putExtra("start_index", position)
            })
        }.also {
            // → Listener per eliminazione singola
            it.onSingleDelete = { photo ->
                photoViewModel.deletePhoto(photo)
            }
        }
        galleryRecycler.adapter = galleryAdapter

        // → Icona e messaggio per la modalità eliminazione multipla
        val deleteIcon = findViewById<ImageView>(R.id.galleryDeleteIcon)
        val deleteMsg = findViewById<TextView>(R.id.galleryDeleteModeMessage)

        // → Gestione del click sull’icona di eliminazione
        deleteIcon.setOnClickListener {
            if (!deleteModeActive) {
                // → Attiva la modalità eliminazione
                deleteModeActive = true
                selectedPhotoIndices.clear()
                deleteMsg.visibility = View.VISIBLE

                // → Abilita la selezione multipla nell'adapter
                galleryAdapter.setSelectionMode(true) { idx, sel ->
                    if (sel) selectedPhotoIndices.add(idx) else selectedPhotoIndices.remove(idx)
                }

            } else {
                if (selectedPhotoIndices.isNotEmpty()) {
                    // → Chiede conferma per eliminare le foto selezionate
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
                    // → Nessuna selezione: mostra messaggio
                    Toast.makeText(this, "Nessuna foto selezionata", Toast.LENGTH_SHORT).show()
                    exitDeleteMode(deleteMsg)
                }
            }
        }

        // → Osserva le foto relative al viaggio e aggiorna la galleria
        photoViewModel.getPhotosForTravel(travelId).observe(this) { photos: List<PhotoEntity> ->
            galleryAdapter.updateList(photos)
        }
    }

    // → Esce dalla modalità eliminazione
    private fun exitDeleteMode(msg: TextView) {
        deleteModeActive = false
        selectedPhotoIndices.clear()
        msg.visibility = View.GONE
        galleryAdapter.setSelectionMode(false, null)
    }
}
