package com.example.globetrotters.activity

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.globetrotters.R
import com.example.globetrotters.database.TravelEntity
import com.example.globetrotters.fragments.DateFragment
import com.example.globetrotters.fragments.PhotoFragment
import com.example.globetrotters.viewmodel.TravelViewModel
import com.google.android.gms.location.LocationServices
import java.util.*

class AddTravelActivity : AppCompatActivity(),
    PhotoFragment.OnPhotoSelectedListener,
    DateFragment.OnDateSelectedListener {

    // ViewModel per gestire i viaggi
    private lateinit var travelViewModel: TravelViewModel

    // Uri della foto selezionata
    private var currentPhotoUri: Uri? = null

    // Fragment per la selezione delle date
    private lateinit var dateFragment: DateFragment

    // Coordinate della posizione selezionata
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    // Launcher per la selezione di coordinate dalla mappa
    private val mapSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Controlla se l'utente ha selezionato coordinate nella mappa
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                selectedLatitude = data.getDoubleExtra("selected_latitude", Double.NaN)
                selectedLongitude = data.getDoubleExtra("selected_longitude", Double.NaN)
                if (!selectedLatitude!!.isNaN() && !selectedLongitude!!.isNaN()) {
                    Toast.makeText(
                        this,
                        "Coordinate selezionate: ($selectedLatitude, $selectedLongitude)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_travel)

        // → Inizializza il ViewModel
        travelViewModel = ViewModelProvider(this)
            .get(TravelViewModel::class.java)

        // → Collega il fragment per la selezione delle date
        dateFragment = supportFragmentManager
            .findFragmentById(R.id.dateFragment) as DateFragment

        // Recupera i riferimenti agli elementi UI
        val titleEditText = findViewById<EditText>(R.id.editTitle)
        val createButton = findViewById<Button>(R.id.createButton)
        val errorMessage = findViewById<TextView>(R.id.errorMessage)
        val locationButton = findViewById<Button>(R.id.locationButton)

        // → Gestione click sul pulsante per selezionare la posizione
        locationButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Seleziona la posizione")
                .setItems(arrayOf("Posizione Attuale", "Scegli dalla mappa")) { _, which ->
                    when (which) {
                        0 -> getCurrentLocation() // → Ottieni posizione attuale
                        1 -> {
                            // → Lancia la mappa per selezionare posizione
                            val intent = Intent(this, MapViewActivity::class.java).apply {
                                putExtra("select_mode", true)
                            }
                            mapSelectionLauncher.launch(intent)
                        }
                    }
                }
                .show()
        }

        // → Gestione click sul pulsante di creazione del viaggio
        createButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val start = dateFragment.getStartDate()
            val end = dateFragment.getEndDate()

            // → Controlli di validazione sul titolo e sulle date
            if (title.isEmpty()) {
                errorMessage.text = "Il titolo non può essere vuoto"
                errorMessage.visibility = TextView.VISIBLE
            } else if (end.before(start)) {
                errorMessage.text = "La data di fine non può essere precedente a quella di inizio"
                errorMessage.visibility = TextView.VISIBLE
            } else {
                // → Crea stringhe formattate per le date
                val startDate = "${start.get(Calendar.DAY_OF_MONTH)}/${start.get(Calendar.MONTH) + 1}/${start.get(Calendar.YEAR)}"
                val endDate = "${end.get(Calendar.DAY_OF_MONTH)}/${end.get(Calendar.MONTH) + 1}/${end.get(Calendar.YEAR)}"

                // → Crea un nuovo oggetto TravelEntity
                val travel = TravelEntity(
                    title = title,
                    dateRange = "$startDate - $endDate",
                    photoUri = currentPhotoUri?.toString(),
                    latitude = selectedLatitude,
                    longitude = selectedLongitude
                )

                // → Usa il ViewModel per salvare il viaggio nel database
                travelViewModel.insertTravel(travel)

                // → Chiudi l'activity e torna indietro
                finish()
            }
        }
    }

    // → Ottiene la posizione attuale dell'utente
    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Permesso posizione non concesso", Toast.LENGTH_LONG).show()
            return
        }

        // → Recupera l'ultima posizione nota
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                selectedLatitude = location.latitude
                selectedLongitude = location.longitude
                Toast.makeText(
                    this,
                    "Coordinate salvate: (${location.latitude}, ${location.longitude})",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(this, "Posizione non disponibile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // → Callback quando viene selezionata una foto dal fragment
    override fun onPhotoUriSelected(uri: Uri) {
        currentPhotoUri = uri
    }

    // → Callback quando vengono selezionate le date
    override fun onDatesSelected(start: Calendar, end: Calendar) {
    }
}
