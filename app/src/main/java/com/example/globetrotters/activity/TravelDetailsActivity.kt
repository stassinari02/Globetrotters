package com.example.globetrotters.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.globetrotters.R
import com.example.globetrotters.adapters.NoteAdapter
import com.example.globetrotters.adapters.PhotoAdapter
import com.example.globetrotters.database.PhotoEntity
import com.example.globetrotters.database.NoteEntity
import com.example.globetrotters.viewmodel.PhotoViewModel
import com.example.globetrotters.viewmodel.NoteViewModel
import java.io.File
import com.example.globetrotters.api.RetrofitInstance
import kotlinx.coroutines.launch

class TravelDetailsActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSIONS_REQUEST_CODE_PHOTO = 200
    }

    private var travelId: Int = 0
    private lateinit var photoAdapter: PhotoAdapter
    private var currentPhotoUri: Uri? = null
    private lateinit var noteAdapter: NoteAdapter

    private lateinit var photoViewModel: PhotoViewModel
    private lateinit var noteViewModel: NoteViewModel

    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            currentPhotoUri?.let { savePhoto(it) }
        }
    }

    private val pickPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
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

        // Inizializza ViewModel
        photoViewModel = ViewModelProvider(this).get(PhotoViewModel::class.java)
        noteViewModel  = ViewModelProvider(this).get(NoteViewModel::class.java)

        travelId = intent.getIntExtra("travel_id", 0)
        findViewById<TextView>(R.id.detailTitleTextView).text =
            intent.getStringExtra("travel_title").orEmpty()

        setupPhotoSection()
        setupNoteSection()

        findViewById<Button>(R.id.openGalleryButton).setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java).apply {
                putExtra("travel_id", travelId)
                putExtra(
                    "travel_title",
                    findViewById<TextView>(R.id.detailTitleTextView).text.toString()
                )
            })
        }

        val wikipediaButton = findViewById<Button>(R.id.openWikipediaButton)
        wikipediaButton.setOnClickListener {
            val city = findViewById<TextView>(R.id.detailTitleTextView).text.toString().trim()
            if (city.isNotEmpty()) {
                fetchWikipediaIntroRetrofit(city)
            } else {
                Toast.makeText(this, "Titolo del viaggio non disponibile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupPhotoSection() {
        val photoRecycler = findViewById<RecyclerView>(R.id.photosRecyclerView)
        photoRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        photoAdapter = PhotoAdapter(emptyList()) { pos ->
            val uris = photoAdapter.getPhotoUris().toTypedArray()
            startActivity(
                Intent(this, FullScreenPhotoActivity::class.java).apply {
                    putExtra("photo_uris", uris)
                    putExtra("start_index", pos)
                }
            )
        }.also {
            it.onSingleDelete = { photo ->
                photoViewModel.deletePhoto(photo)
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
                            photoAdapter.getSelectedItems().forEach {
                                photoViewModel.deletePhoto(it)
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

        // Osserva LiveData dal ViewModel
        photoViewModel.getPhotosForTravel(travelId).observe(this) {
            photoAdapter.updateList(it)
        }

        findViewById<Button>(R.id.takePhotoButton).setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val file = File.createTempFile("travel_", ".jpg", cacheDir)
                currentPhotoUri =
                    FileProvider.getUriForFile(this, "$packageName.provider", file)
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
                }
                takePhotoLauncher.launch(intent)
            } else {
                Toast.makeText(this, "Permesso fotocamera non concesso", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        findViewById<Button>(R.id.loadPhotoButton).setOnClickListener {
            val readPerm = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

            if (ContextCompat.checkSelfPermission(this, readPerm) == PackageManager.PERMISSION_GRANTED) {
                pickPhotoLauncher.launch("image/*")
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(readPerm),
                    PERMISSIONS_REQUEST_CODE_PHOTO
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE_PHOTO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
        photoViewModel.insertPhoto(PhotoEntity(travelId = travelId, uri = uri.toString()))
        Toast.makeText(this, "Foto salvata", Toast.LENGTH_SHORT).show()
    }

    private fun setupNoteSection() {
        val notesRecycler = findViewById<RecyclerView>(R.id.notesRecyclerView)
        notesRecycler.layoutManager = LinearLayoutManager(this)
        noteAdapter = NoteAdapter(emptyList()).also {
            it.onSingleDelete = { note ->
                noteViewModel.deleteNote(note)
            }
        }
        notesRecycler.adapter = noteAdapter

        // Osserva tutte le note inizialmente
        noteViewModel.getNotesForTravel(travelId)
            .observe(this) { noteAdapter.updateList(it) }

        // Search-as-you-type per le note
        val searchBar = findViewById<EditText>(R.id.searchNoteEditText)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString()?.trim().orEmpty()
                if (q.isEmpty()) {
                    noteViewModel.getNotesForTravel(travelId)
                        .observe(this@TravelDetailsActivity) { noteAdapter.updateList(it) }
                } else {
                    noteViewModel.searchNotes(travelId, q)
                        .observe(this@TravelDetailsActivity) { noteAdapter.updateList(it) }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })

        findViewById<ImageButton>(R.id.addNoteButton).setOnClickListener {
            val input = EditText(this).apply { hint = "Scrivi la tua nota" }
            AlertDialog.Builder(this)
                .setTitle("Nuova Nota")
                .setView(input)
                .setNegativeButton("Annulla", null)
                .setPositiveButton("Aggiungi") { _, _ ->
                    val text = input.text.toString().trim()
                    if (text.isNotEmpty()) {
                        noteViewModel.insertNote(NoteEntity(travelId = travelId, text = text))
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
                            noteAdapter.getSelectedItems().forEach {
                                noteViewModel.deleteNote(it)
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

    private fun fetchWikipediaIntroRetrofit(city: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.wikipediaApi.getIntroExtract(titles = city)
                if (response.isSuccessful) {
                    val body = response.body()
                    val pages = body?.query?.pages
                    if (pages != null && pages.isNotEmpty()) {
                        val page = pages.values.first()
                        val extract = page.extract ?: "Nessuna informazione trovata."
                        AlertDialog.Builder(this@TravelDetailsActivity)
                            .setTitle(city)
                            .setMessage(extract)
                            .setPositiveButton("OK", null)
                            .show()
                    } else {
                        Toast.makeText(this@TravelDetailsActivity, "Pagina non trovata", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@TravelDetailsActivity, "Errore API Wikipedia", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TravelDetailsActivity, "Errore: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

}
