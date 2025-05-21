package com.example.globetrotters

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spanned
import android.view.View
import android.widget.*
import android.text.SpannableString
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.globetrotters.adapters.TravelAdapter
import android.text.style.StyleSpan
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Observer
import com.example.globetrotters.database.TravelDatabase
import com.example.globetrotters.database.TravelEntity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TravelAdapter
    private var fullTravelList: List<TravelEntity> = emptyList()
    private val PERMISSIONS_REQUEST_CODE = 100
    private var deleteModeActive = false
    private var selectedItems = mutableSetOf<Int>()
    private var deleteClickCounter = 0
    private lateinit var db: TravelDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        db = TravelDatabase.getDatabase(this)

        adapter = TravelAdapter(emptyList()) { travel ->
            lifecycleScope.launch {
                db.travelDao().deleteTravel(travel)
            }
        }
        recyclerView.adapter = adapter

        // Osserva i dati dal database e aggiorna l'adapter
        db.travelDao().getAllTravels().observe(this, Observer { travels ->
            fullTravelList = travels
            adapter.updateList(travels)
        })

        // Icona per le settings
        val settingsIcon: ImageView = findViewById(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val searchEditText: EditText = findViewById(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s.toString().lowercase()
                val filteredList = fullTravelList.filter { it.title.lowercase().contains(query) }
                adapter.updateList(filteredList)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        checkPermissions()

        // Gestione eliminazione (delete mode)
        val deleteIcon: ImageView = findViewById(R.id.deleteIcon)
        val deleteMessage: TextView = findViewById(R.id.deleteModeMessage)
        val superTitle = findViewById<TextView>(R.id.superTitleTextView)
        val contentTop = superTitle.parent as LinearLayout

        deleteIcon.setOnClickListener {
            deleteClickCounter++
            if (!deleteModeActive) {
                deleteModeActive = true
                selectedItems.clear()
                deleteMessage.visibility = View.VISIBLE
                adapter.setSelectionMode(true) { index, selected ->
                    if (selected) selectedItems.add(index) else selectedItems.remove(index)
                }
                // Uscita dalla modalità eliminazione se clicchi fuori
                contentTop.setOnClickListener { exitDeleteMode() }
            } else {
                // Mostra popup di conferma per l'eliminazione
                if (selectedItems.isNotEmpty()) {
                    val selectedTravels = adapter.getSelectedItems()
                    val selectedTitles = selectedTravels.map { it.title }
                    val message = SpannableString("Sicuro di voler eliminare: ${selectedTitles.joinToString(", ")}")
                    val start = message.indexOf(":") + 2
                    val end = message.length
                    message.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    AlertDialog.Builder(this)
                        .setTitle("Conferma eliminazione")
                        .setMessage(message)
                        .setPositiveButton("Si") { dialog, _ ->
                            val idsToDelete = selectedTravels.map { it.id }
                            lifecycleScope.launch {
                                db.travelDao().deleteTravelsByIds(idsToDelete)
                            }
                            exitDeleteMode()
                            dialog.dismiss()
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            exitDeleteMode()
                            dialog.dismiss()
                        }
                        .show()
                } else {
                    Toast.makeText(this, "Nessun viaggio selezionato", Toast.LENGTH_SHORT).show()
                }
            }
        }
        val mapIcon: ImageView = findViewById(R.id.mapIcon)
        mapIcon.setOnClickListener {
            startActivity(Intent(this, MapViewActivity::class.java))
        }

        val newTravelButton: Button = findViewById(R.id.addTravelButton)
        newTravelButton.setOnClickListener {
            val intent = Intent(this, AddTravelActivity::class.java)
            startActivity(intent)
        }

    }

    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Controlla entrambi per compatibilità con Android < 13 e >= 13
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            val deniedPermanently = permissions.indices.any { index ->
                grantResults[index] != PackageManager.PERMISSION_GRANTED &&
                        !ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[index])
            }

            if (deniedPermanently) {
            } else {
                val deniedNormally = grantResults.indices.any { grantResults[it] != PackageManager.PERMISSION_GRANTED }
                if (deniedNormally) {
                    Toast.makeText(this, "Permessi necessari per fotocamera e GPS", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun exitDeleteMode() {
        deleteModeActive = false
        deleteClickCounter = 0
        selectedItems.clear()
        findViewById<TextView>(R.id.deleteModeMessage).visibility = View.GONE
        adapter.setSelectionMode(false, null)
        val superTitle = findViewById<TextView>(R.id.superTitleTextView)
        val contentTop = superTitle.parent as LinearLayout
        contentTop.setOnClickListener(null) // disattiva uscita touch
        }
}