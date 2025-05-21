package com.example.globetrotters

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.lifecycle.lifecycleScope
import com.example.globetrotters.database.TravelDatabase
import com.example.globetrotters.database.TravelEntity
import com.example.globetrotters.fragments.DateFragment
import com.example.globetrotters.fragments.PhotoFragment
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.util.*

class AddTravelActivity : AppCompatActivity(),
    PhotoFragment.OnPhotoSelectedListener,
    DateFragment.OnDateSelectedListener {

    private lateinit var db: TravelDatabase
    private var currentPhotoUri: Uri? = null
    private lateinit var dateFragment: DateFragment
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    private val mapSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
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

        db = TravelDatabase.getDatabase(this)

        dateFragment = supportFragmentManager.findFragmentById(R.id.dateFragment) as DateFragment

        val titleEditText = findViewById<EditText>(R.id.editTitle)
        val createButton = findViewById<Button>(R.id.createButton)
        val errorMessage = findViewById<TextView>(R.id.errorMessage)
        val locationButton = findViewById<Button>(R.id.locationButton)
        locationButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Seleziona la posizione")
                .setItems(arrayOf("Posizione Attuale", "Scegli dalla mappa")) { _, which ->
                    when (which) {
                        0 -> getCurrentLocation()
                        1 -> {
                            // 2) lancio MapViewActivity in modalità "select"
                            val intent = Intent(this, MapViewActivity::class.java).apply {
                                putExtra("select_mode", true)
                            }
                            mapSelectionLauncher.launch(intent)
                        }
                    }
                }
                .show()
        }

        createButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val start = dateFragment.getStartDate()
            val end = dateFragment.getEndDate()

            if (title.isEmpty()) {
                errorMessage.text = "Il titolo non può essere vuoto"
                errorMessage.visibility = TextView.VISIBLE
            } else if (end.before(start)) {
                errorMessage.text = "La data di fine non può essere precedente a quella di inizio"
                errorMessage.visibility = TextView.VISIBLE
            } else {
                val startDate =
                    "${start.get(Calendar.DAY_OF_MONTH)}/${start.get(Calendar.MONTH) + 1}/${
                        start.get(Calendar.YEAR)
                    }"
                val endDate = "${end.get(Calendar.DAY_OF_MONTH)}/${end.get(Calendar.MONTH) + 1}/${
                    end.get(Calendar.YEAR)
                }"
                val travel = TravelEntity(
                    title = title,
                    dateRange = "$startDate - $endDate",
                    photoUri = currentPhotoUri?.toString(),
                    latitude = selectedLatitude,
                    longitude = selectedLongitude
                )


                lifecycleScope.launch {
                    db.travelDao().insertTravel(travel)
                    finish()
                }
            }
        }
    }

    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permesso posizione non concesso", Toast.LENGTH_LONG).show()
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                selectedLatitude = location.latitude
                selectedLongitude = location.longitude
                Toast.makeText(this, "Coordinate salvate: (${location.latitude}, ${location.longitude})", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Posizione non disponibile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPhotoUriSelected(uri: Uri) {
        currentPhotoUri = uri
    }

    override fun onDatesSelected(start: Calendar, end: Calendar){

       }

}