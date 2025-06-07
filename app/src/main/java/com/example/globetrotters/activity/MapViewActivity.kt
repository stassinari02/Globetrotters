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

class MapViewActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private lateinit var travelViewModel: TravelViewModel

    private var selectMode = false
    private var pickedLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_view)

        travelViewModel = ViewModelProvider(this)
            .get(TravelViewModel::class.java)

        selectMode = intent.getBooleanExtra("select_mode", false)

        findViewById<Button>(R.id.confirmButton).apply {
            visibility = if (selectMode) View.VISIBLE else View.GONE
            setOnClickListener {
                pickedLatLng?.let { latLng ->
                    val data = Intent().apply {
                        putExtra("selected_latitude", latLng.latitude)
                        putExtra("selected_longitude", latLng.longitude)
                    }
                    setResult(RESULT_OK, data)
                }
                finish()
            }
        }

        setupSearch()
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
            .getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (selectMode) {
            mMap.setOnMapClickListener { latLng -> placeSelectionMarker(latLng) }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(20.0, 0.0), 1f))
        } else {
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
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
                } else {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(20.0, 0.0), 1f))
                }
            }
        }
    }

    private fun placeSelectionMarker(latLng: LatLng) {
        mMap.clear()
        val marker = mMap.addMarker(MarkerOptions().position(latLng).title("Selezionato"))
        marker?.showInfoWindow()
        pickedLatLng = latLng
    }


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
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 12f))
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