package com.example.globetrotters.activity

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import com.example.globetrotters.R
import com.example.globetrotters.viewmodel.TravelViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

// Attività che mostra una mappa con i marker dei viaggi o consente di selezionare una posizione
class MapViewActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap  // Oggetto mappa Google

    private lateinit var travelViewModel: TravelViewModel  // ViewModel per ottenere i dati dei viaggi

    private var selectMode = false       // True se siamo in modalità selezione coordinate
    private var pickedLatLng: LatLng? = null  // Coordinate selezionate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_view)

        // Inizializza il ViewModel
        travelViewModel = ViewModelProvider(this)
            .get(TravelViewModel::class.java)

        // Recupera il flag dalla chiamata dell’intent: true se la mappa serve per scegliere una posizione
        selectMode = intent.getBooleanExtra("select_mode", false)

        // Bottone di conferma visibile solo in modalità selezione
        findViewById<Button>(R.id.confirmButton).apply {
            visibility = if (selectMode) View.VISIBLE else View.GONE
            setOnClickListener {
                // Se è stata selezionata una posizione, la restituisce come risultato all’activity chiamante
                pickedLatLng?.let { latLng ->
                    val data = Intent().apply {
                        putExtra("selected_latitude", latLng.latitude)
                        putExtra("selected_longitude", latLng.longitude)
                    }
                    setResult(RESULT_OK, data)
                }
                finish()  // Termina l’activity
            }
        }

        // Configura la barra di ricerca
        setupSearch()

        // Carica la mappa asincrona
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
            .getMapAsync(this)
    }

    // Chiamata automatica quando la mappa è pronta
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (selectMode) {
            // In modalità selezione: consente il tap sulla mappa per piazzare un marker
            mMap.setOnMapClickListener { latLng -> placeSelectionMarker(latLng) }

            // Posizione iniziale della mappa "neutrale"
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(20.0, 0.0), 1f))

        } else {
            // In modalità normale: mostra i marker dei viaggi esistenti
            travelViewModel.getTravelsWithCoordinates { travels ->
                if (travels.isNotEmpty()) {
                    val bounds = LatLngBounds.Builder()

                    travels.forEach { t ->
                        val pos = LatLng(t.latitude!!, t.longitude!!)
                        val marker = mMap.addMarker(
                            MarkerOptions()
                                .position(pos)
                                .title(t.title)
                                .anchor(0.5f, 0.5f)
                        )
                        marker?.showInfoWindow()
                        bounds.include(pos)
                    }

                    // Centra la mappa per contenere tutti i marker
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
                } else {
                    // Se non ci sono viaggi, mostra la mappa "neutrale"
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(20.0, 0.0), 1f))
                }
            }
        }
    }

    // Aggiunge un marker sulla mappa e salva la posizione selezionata
    private fun placeSelectionMarker(latLng: LatLng) {
        mMap.clear()  // Rimuove eventuali marker precedenti
        val marker = mMap.addMarker(MarkerOptions().position(latLng).title("Selezionato"))
        marker?.showInfoWindow()
        pickedLatLng = latLng
    }

    // Configura la SearchView per cercare un luogo da nome
    private fun setupSearch() {
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val geocoder = Geocoder(this@MapViewActivity, Locale.getDefault())
                    val list = geocoder.getFromLocationName(it, 1)

                    if (!list.isNullOrEmpty()) {
                        val addr = list[0]
                        val pos = LatLng(addr.latitude, addr.longitude)

                        // Sposta la mappa sul risultato della ricerca
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 12f))

                        // In modalità selezione piazza anche il marker
                        if (selectMode) {
                            placeSelectionMarker(pos)
                        }
                    } else {
                        Toast.makeText(this@MapViewActivity, "Luogo non trovato", Toast.LENGTH_SHORT).show()
                    }
                }
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?) = false
        })
    }
}
