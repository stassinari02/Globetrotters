package com.example.globetrotters

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.globetrotters.adapters.NoteAdapter
import com.example.globetrotters.adapters.PhotoAdapter
import com.example.globetrotters.database.NoteEntity
import com.example.globetrotters.database.PhotoEntity
import com.example.globetrotters.database.TravelDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class TravelDetailsActivity : AppCompatActivity() {

    private lateinit var db: TravelDatabase
    private var travelId: Int = 0

    private lateinit var photoAdapter: PhotoAdapter
    private var currentPhotoUri: Uri? = null

    private lateinit var noteAdapter: NoteAdapter

    // launcher per scatto e pick
    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) currentPhotoUri?.let { savePhoto(it) }
    }
    private val pickPhotoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val dst = File(filesDir, "photo_${System.currentTimeMillis()}.jpg")
            contentResolver.openInputStream(it)?.use { input ->
                dst.outputStream().use { out -> input.copyTo(out) }
            }
            savePhoto(Uri.fromFile(dst))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_details)

        db = TravelDatabase.getDatabase(this)

        travelId = intent.getIntExtra("travel_id", 0)
        findViewById<TextView>(R.id.detailTitleTextView).text =
            intent.getStringExtra("travel_title").orEmpty()

        // — FOTO —
        setupPhotoSection()
        // — NOTE —
        setupNoteSection()

        findViewById<Button>(R.id.openGalleryButton).setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java).apply {
                putExtra("travel_id", travelId)
                putExtra("travel_title", findViewById<TextView>(R.id.detailTitleTextView).text.toString())
            })
        }
    }

    private fun setupPhotoSection() {
        val photoRecycler = findViewById<RecyclerView>(R.id.photosRecyclerView)
        photoRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        photoAdapter = PhotoAdapter(emptyList()) { pos ->
            val uris = photoAdapter.getPhotoUris().toTypedArray()
            startActivity(Intent(this, FullScreenPhotoActivity::class.java).apply {
                putExtra("photo_uris", uris)
                putExtra("start_index", pos)
            })
        }.also {
            it.onSingleDelete = { photo ->
                lifecycleScope.launch { db.photoDao().deletePhoto(photo) }
            }
        }
        photoRecycler.adapter = photoAdapter

        val deleteIcon = findViewById<ImageView>(R.id.photoDeleteIcon)
        val deleteMsg = findViewById<TextView>(R.id.photoDeleteModeMessage)
        var deleteMode = false
        val selected = mutableSetOf<Int>()

        deleteIcon.setOnClickListener {
            if (!deleteMode) {
                deleteMode = true
                selected.clear()
                deleteMsg.visibility = View.VISIBLE
                photoAdapter.setSelectionMode(true) { idx, sel ->
                    if (sel) selected.add(idx) else selected.remove(idx)
                }
            } else {
                if (selected.isNotEmpty()) {
                    AlertDialog.Builder(this)
                        .setTitle("Conferma eliminazione")
                        .setMessage("Sicuro di voler eliminare ${selected.size} foto?")
                        .setPositiveButton("Si") { _, _ ->
                            lifecycleScope.launch {
                                photoAdapter.getSelectedItems().forEach { db.photoDao().deletePhoto(it) }
                            }
                            exitPhotoDeleteMode(deleteMsg)
                            deleteMode = false
                        }
                        .setNegativeButton("No") { _, _ ->
                            exitPhotoDeleteMode(deleteMsg)
                            deleteMode = false
                        }
                        .show()
                } else {
                    Toast.makeText(this, "Nessuna foto selezionata", Toast.LENGTH_SHORT).show()
                    exitPhotoDeleteMode(deleteMsg)
                    deleteMode = false
                }
            }
        }

        db.photoDao().getPhotosForTravel(travelId).observe(this) {
            photoAdapter.updateList(it)
        }

        findViewById<Button>(R.id.takePhotoButton).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val file = File.createTempFile("travel_", ".jpg", cacheDir)
                currentPhotoUri = FileProvider.getUriForFile(this, "$packageName.provider", file)
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
                }
                takePhotoLauncher.launch(intent)
            } else {
                Toast.makeText(this, "Permesso fotocamera non concesso", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<Button>(R.id.loadPhotoButton).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                pickPhotoLauncher.launch("image/*")
            } else {
                Toast.makeText(this, "Permesso galleria non concesso", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exitPhotoDeleteMode(msg: TextView) {
        msg.visibility = View.GONE
        photoAdapter.setSelectionMode(false, null)
    }

    private fun savePhoto(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.photoDao().insertPhoto(PhotoEntity(travelId = travelId, uri = uri.toString()))
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TravelDetailsActivity, "Foto salvata", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TravelDetailsActivity, "Errore salvataggio", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupNoteSection() {
        val notesRecycler = findViewById<RecyclerView>(R.id.notesRecyclerView)
        notesRecycler.layoutManager = LinearLayoutManager(this)
        noteAdapter = NoteAdapter(emptyList()).also {
            it.onSingleDelete = { note ->
                lifecycleScope.launch { db.noteDao().deleteNote(note) }
            }
        }
        notesRecycler.adapter = noteAdapter

        val noteDao = db.noteDao()
        noteDao.getNotesForTravel(travelId).observe(this) {
            noteAdapter.updateList(it)
        }

        val searchBar = findViewById<EditText>(R.id.searchNoteEditText)
        // apri dialog per la ricerca
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
                    val q = dialogInput.text.toString().trim()
                    if (q.isEmpty()) {
                        noteDao.getNotesForTravel(travelId).observe(this) { noteAdapter.updateList(it) }
                    } else {
                        noteDao.searchNotes(travelId, q).observe(this) { noteAdapter.updateList(it) }
                    }
                }
                .show()
        }

        findViewById<ImageButton>(R.id.addNoteButton).setOnClickListener {
            val input = EditText(this).apply { hint = "Scrivi la tua nota" }
            AlertDialog.Builder(this)
                .setTitle("Nuova Nota")
                .setView(input)
                .setNegativeButton("Annulla", null)
                .setPositiveButton("Aggiungi") { _, _ ->
                    val text = input.text.toString().trim()
                    if (text.isNotEmpty()) lifecycleScope.launch {
                        noteDao.insertNote(NoteEntity(travelId = travelId, text = text))
                    }
                }
                .show()
        }

        val noteDeleteIcon = findViewById<ImageView>(R.id.noteDeleteIcon)
        val noteDeleteMsg = findViewById<TextView>(R.id.noteDeleteModeMessage)
        var noteDeleteMode = false
        val selectedNote = mutableSetOf<Int>()

        noteDeleteIcon.setOnClickListener {
            if (!noteDeleteMode) {
                noteDeleteMode = true
                selectedNote.clear()
                noteDeleteMsg.visibility = View.VISIBLE
                noteAdapter.setSelectionMode(true) { idx, sel ->
                    if (sel) selectedNote.add(idx) else selectedNote.remove(idx)
                }
            } else {
                if (selectedNote.isNotEmpty()) {
                    AlertDialog.Builder(this)
                        .setTitle("Conferma eliminazione")
                        .setMessage("Vuoi cancellare queste ${selectedNote.size} note?")
                        .setPositiveButton("Si") { _, _ ->
                            lifecycleScope.launch {
                                noteAdapter.getSelectedItems().forEach { db.noteDao().deleteNote(it) }
                            }
                            exitNoteDeleteMode(noteDeleteMsg)
                            noteDeleteMode = false
                        }
                        .setNegativeButton("No") { _, _ ->
                            exitNoteDeleteMode(noteDeleteMsg)
                            noteDeleteMode = false
                        }
                        .show()
                } else {
                    Toast.makeText(this, "Nessuna nota selezionata", Toast.LENGTH_SHORT).show()
                    exitNoteDeleteMode(noteDeleteMsg)
                    noteDeleteMode = false
                }
            }
        }
    }

    private fun exitNoteDeleteMode(msg: TextView) {
        msg.visibility = View.GONE
        noteAdapter.setSelectionMode(false, null)
    }
}
