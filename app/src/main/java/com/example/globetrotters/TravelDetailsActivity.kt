package com.example.globetrotters

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.globetrotters.adapters.PhotoAdapter
import com.example.globetrotters.database.PhotoEntity
import com.example.globetrotters.database.TravelDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import com.example.globetrotters.adapters.NoteAdapter
import com.example.globetrotters.database.NoteEntity


class TravelDetailsActivity : AppCompatActivity() {

    private lateinit var db: TravelDatabase
    private var travelId: Int = 0
    private lateinit var photoAdapter: PhotoAdapter
    private var currentPhotoUri: Uri? = null

    // Launcher per scattare foto
    private val takePhotoLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        // *NON* chiudere l’Activity qui
        if (result.resultCode == RESULT_OK) {
            currentPhotoUri?.let { savePhoto(it) }
        }
    }

    // Launcher per caricare da galleria
    private val pickPhotoLauncher = registerForActivityResult(GetContent(),
        ActivityResultCallback { uri ->
            uri?.let {
                // Copia in internal storage
                val dst = File(filesDir, "photo_${System.currentTimeMillis()}.jpg")
                contentResolver.openInputStream(it)?.use { input ->
                    dst.outputStream().use { out -> input.copyTo(out) }
                }
                savePhoto(Uri.fromFile(dst))
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_details)

        // --- Database ---
        db = TravelDatabase.getDatabase(this)

        // --- Recupera id e titolo ---
        travelId = intent.getIntExtra("travel_id", 0)
        findViewById<TextView>(R.id.detailTitleTextView)
            .text = intent.getStringExtra("travel_title").orEmpty()

        // --- FOTO: RecyclerView orizzontale ---
        val photoRecycler = findViewById<RecyclerView>(R.id.photosRecyclerView)
        photoRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        photoAdapter = PhotoAdapter(emptyList()) { position ->
            // Click breve → fullscreen
            val uris = photoAdapter.getPhotoUris().toTypedArray()
            val intent = Intent(this, FullScreenPhotoActivity::class.java).apply {
                putExtra("photo_uris", uris)
                putExtra("start_index", position)
            }
            startActivity(intent)
        }.apply {
            // Long-click conferma delete singola
            onSingleDelete = { photo ->
                lifecycleScope.launch {
                    db.photoDao().deletePhoto(photo)
                }
            }
        }
        photoRecycler.adapter = photoAdapter

        // Multi-delete foto
        val deleteIcon = findViewById<ImageView>(R.id.photoDeleteIcon)
        val deleteMsg = findViewById<TextView>(R.id.photoDeleteModeMessage)
        var deleteModeActive = false
        val selectedPhotoIndices = mutableSetOf<Int>()

        deleteIcon.setOnClickListener {
            if (!deleteModeActive) {
                deleteModeActive = true
                deleteMsg.visibility = View.VISIBLE
                photoAdapter.setSelectionMode(true) { idx, sel ->
                    if (sel) selectedPhotoIndices.add(idx) else selectedPhotoIndices.remove(idx)
                }
            } else {
                if (selectedPhotoIndices.isNotEmpty()) {
                    AlertDialog.Builder(this)
                        .setTitle("Conferma eliminazione")
                        .setMessage("Sicuro di volere eliminare ${selectedPhotoIndices.size} foto?")
                        .setPositiveButton("Si") { _, _ ->
                            lifecycleScope.launch {
                                photoAdapter.getSelectedItems()
                                    .forEach { db.photoDao().deletePhoto(it) }
                            }
                            deleteMsg.visibility = View.GONE
                            photoAdapter.setSelectionMode(false, null)
                            deleteModeActive = false
                        }
                        .setNegativeButton("No") { _, _ ->
                            deleteMsg.visibility = View.GONE
                            photoAdapter.setSelectionMode(false, null)
                            deleteModeActive = false
                        }
                        .show()
                } else {
                    Toast.makeText(this, "Nessuna foto selezionata", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe foto LiveData
        db.photoDao().getPhotosForTravel(travelId).observe(this) {
            photoAdapter.updateList(it)
        }

        // --- Pulsanti Scatta/Carica foto ---
        findViewById<Button>(R.id.takePhotoButton).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
                val photoFile = File.createTempFile("travel_", ".jpg", cacheDir)
                currentPhotoUri = FileProvider.getUriForFile(
                    this,
                    "$packageName.provider",
                    photoFile
                )
                val capture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    .apply { putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri) }
                takePhotoLauncher.launch(capture)
            } else {
                Toast.makeText(this, "Permesso fotocamera non concesso", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.loadPhotoButton).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
                pickPhotoLauncher.launch("image/*")
            } else {
                Toast.makeText(this, "Permesso galleria non concesso", Toast.LENGTH_SHORT).show()
            }
        }

        // --- NOTE: RecyclerView verticale ---

        val notesRecycler = findViewById<RecyclerView>(R.id.notesRecyclerView)
        notesRecycler.layoutManager = LinearLayoutManager(this)
        val noteAdapter = NoteAdapter(emptyList())
        notesRecycler.adapter = noteAdapter

        // Observe note LiveData
        val noteDao = db.noteDao()
        noteDao.getNotesForTravel(travelId).observe(this) {
            noteAdapter.updateList(it)
        }

        // Barra ricerca note: apertura dialog per digitazione
        val searchBar = findViewById<EditText>(R.id.searchNoteEditText)
        searchBar.isFocusable = false
        searchBar.isClickable = true
        searchBar.setOnClickListener {
            val dialogInput = EditText(this).apply {
                setText(searchBar.text.toString())
                hint = "Cerca note..."
            }
            AlertDialog.Builder(this)
                .setTitle("Cerca Note")
                .setView(dialogInput)
                .setNegativeButton("Annulla", null)
                .setPositiveButton("Cerca") { _, _ ->
                    val query = dialogInput.text.toString().trim()
                    searchBar.setText(query)
                    if (query.isEmpty()) {
                        noteDao.getNotesForTravel(travelId)
                            .observe(this) { noteAdapter.updateList(it) }
                    } else {
                        noteDao.searchNotes(travelId, query)
                            .observe(this) { noteAdapter.updateList(it) }
                    }
                }
                .show()
        }

        // Pulsante aggiungi nota
        findViewById<ImageButton>(R.id.addNoteButton).setOnClickListener {
            val input = EditText(this).apply { hint = "Scrivi la tua nota" }
            AlertDialog.Builder(this)
                .setTitle("Nuova Nota")
                .setView(input)
                .setNegativeButton("Annulla", null)
                .setPositiveButton("Aggiungi") { _, _ ->
                    val text = input.text.toString().trim()
                    if (text.isNotEmpty()) {
                        lifecycleScope.launch {
                            noteDao.insertNote(NoteEntity(travelId = travelId, text = text))
                        }
                    }
                }
                .show()
        }
        val noteDeleteIcon = findViewById<ImageView>(R.id.noteDeleteIcon)
        val noteDeleteMsg = findViewById<TextView>(R.id.noteDeleteModeMessage)
        var noteDeleteMode = false
        val selectedNoteIndices = mutableSetOf<Int>()

        // configura adapter per note
        noteAdapter.onSingleDelete = { note ->
            lifecycleScope.launch { db.noteDao().deleteNote(note) }
        }

        noteDeleteIcon.setOnClickListener {
            if (!noteDeleteMode) {
                noteDeleteMode = true
                noteDeleteMsg.visibility = View.VISIBLE
                noteAdapter.setSelectionMode(true) { idx, sel ->
                    if (sel) selectedNoteIndices.add(idx) else selectedNoteIndices.remove(idx)
                }
            } else {
                if (selectedNoteIndices.isNotEmpty()) {
                    AlertDialog.Builder(this)
                        .setTitle("Vuoi cancellare queste ${selectedNoteIndices.size} note?")
                        .setPositiveButton("OK") { _, _ ->
                            lifecycleScope.launch {
                                noteAdapter.getSelectedItems()
                                    .forEach { db.noteDao().deleteNote(it) }
                            }
                            noteDeleteMsg.visibility = View.GONE
                            noteAdapter.setSelectionMode(false, null)
                            noteDeleteMode = false
                        }
                        .setNegativeButton("Annulla") { _, _ ->
                            noteDeleteMsg.visibility = View.GONE
                            noteAdapter.setSelectionMode(false, null)
                            noteDeleteMode = false
                        }
                        .show()
                } else {
                    Toast.makeText(this, "Nessuna nota selezionata", Toast.LENGTH_SHORT).show()
                }
            }
        }
        findViewById<Button>(R.id.openGalleryButton).setOnClickListener {
            val intent = Intent(this, GalleryActivity::class.java).apply {
                putExtra("travel_id", travelId)
                putExtra(
                    "travel_title",
                    findViewById<TextView>(R.id.detailTitleTextView).text.toString()
                )
            }
            startActivity(intent)
        }
    }

    // Salvataggio in DB con Toast di conferma/errore
    private fun savePhoto(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.photoDao().insertPhoto(PhotoEntity(travelId = travelId, uri = uri.toString()))
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@TravelDetailsActivity,
                        "Foto salvata correttamente nel database",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@TravelDetailsActivity,
                        "Problemi nel salvataggio della foto",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun exitPhotoDeleteMode(msg: TextView) {
        msg.visibility = View.GONE
        photoAdapter.setSelectionMode(false, null)
        }
}