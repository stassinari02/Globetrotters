package com.example.globetrotters

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.globetrotters.adapters.TravelAdapter
import com.example.globetrotters.models.TravelItem
import android.app.AlertDialog
import android.provider.Settings
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TravelAdapter
    private var photoUri: Uri? = null
    private val CAMERA_REQUEST_CODE = 101
    private val travelList = mutableListOf<TravelItem>()
    private val PERMISSIONS_REQUEST_CODE = 100
    var currentPhotoUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configura il RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        adapter = TravelAdapter(travelList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Bottone per aggiungere un viaggio
        val addTravelButton: Button = findViewById(R.id.addTravelButton)
        addTravelButton.setOnClickListener {
            showAddTravelDialog()
        }

        // Gestione del clic sull'icona delle impostazioni
        val settingsIcon: ImageView = findViewById(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            // Avvia l'activity delle impostazioni
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Verifica e chiedi permessi se non concessi
        checkPermissions()
    }

    private fun showAddTravelDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_travel, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.editTitle)
        val startDatePicker = dialogView.findViewById<DatePicker>(R.id.startDatePicker)
        val endDatePicker = dialogView.findViewById<DatePicker>(R.id.endDatePicker)
        val createButton = dialogView.findViewById<Button>(R.id.createButton)
        val errorMessage = dialogView.findViewById<TextView>(R.id.errorMessage)
        val takePhotoButton = dialogView.findViewById<Button>(R.id.takePhotoButton)
        val photoPreview = dialogView.findViewById<ImageView>(R.id.photoPreview)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        takePhotoButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                val photoFile = File.createTempFile("travel_photo_", ".jpg", cacheDir)

                // Usa FileProvider per ottenere un URI sicuro
                currentPhotoUri = FileProvider.getUriForFile(
                    this,
                    "com.example.globetrotters.provider",  // Devi usare lo stesso nome
                    photoFile
                )


                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, currentPhotoUri)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            } else {
                Toast.makeText(this, "Permesso fotocamera non concesso", Toast.LENGTH_SHORT).show()
            }
        }

        // Imposta la data minima iniziale per endDatePicker (uguale a startDatePicker)
        val startCalendarInit = java.util.Calendar.getInstance().apply {
            set(startDatePicker.year, startDatePicker.month, startDatePicker.dayOfMonth)
        }
        endDatePicker.minDate = startCalendarInit.timeInMillis

        // Quando l'utente tocca il secondo DatePicker, aggiorna il minDate
        endDatePicker.setOnTouchListener { _, _ ->
            val startCalendar = java.util.Calendar.getInstance().apply {
                set(startDatePicker.year, startDatePicker.month, startDatePicker.dayOfMonth)
            }
            endDatePicker.minDate = startCalendar.timeInMillis
            false
        }

        createButton.setOnClickListener {

            val title = titleEditText.text.toString().trim()

            val startCalendar = java.util.Calendar.getInstance().apply {
                set(startDatePicker.year, startDatePicker.month, startDatePicker.dayOfMonth)
            }

            val endCalendar = java.util.Calendar.getInstance().apply {
                set(endDatePicker.year, endDatePicker.month, endDatePicker.dayOfMonth)
            }

            if (title.isEmpty()) {
                errorMessage.text = "Il titolo non può essere vuoto"
                errorMessage.visibility = View.VISIBLE
            } else if (endCalendar.before(startCalendar)) {
                errorMessage.text = "La data di fine non può essere precedente a quella di inizio"
                errorMessage.visibility = View.VISIBLE
            } else {
                val startDate = "${startDatePicker.dayOfMonth}/${startDatePicker.month + 1}/${startDatePicker.year}"
                val endDate = "${endDatePicker.dayOfMonth}/${endDatePicker.month + 1}/${endDatePicker.year}"

                val travelItem = TravelItem(title, "$startDate - $endDate", currentPhotoUri?.toString())
                adapter.addItem(travelItem)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    // Verifica se i permessi necessari sono concessi
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Se i permessi non sono concessi, chiedili
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            Toast.makeText(this, "Permessi già concessi", Toast.LENGTH_SHORT).show()
        }
    }

    // Gestisci la risposta alla richiesta dei permessi
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // Mostra un avviso che i permessi sono necessari
                Toast.makeText(this, "Permessi necessari per fotocamera e GPS", Toast.LENGTH_LONG).show()

                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            currentPhotoUri?.let { uri ->
                // Mostra un toast per testare
                Toast.makeText(this, "Foto salvata: $uri", Toast.LENGTH_SHORT).show()

                // Se vuoi aggiornare direttamente l'anteprima, puoi farlo nel dialog
                val dialogView = layoutInflater.inflate(R.layout.dialog_add_travel, null)
                val preview = dialogView.findViewById<ImageView>(R.id.photoPreview)
                preview?.setImageURI(uri)
            }
        }
    }
}

//rende in grigino le date, ma da un errore da console, non supporta la versione di api 26
//private fun showAddTravelDialog() {
//        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_travel, null)
//        val titleEditText = dialogView.findViewById<EditText>(R.id.editTitle)
//        val startDatePicker = dialogView.findViewById<DatePicker>(R.id.startDatePicker)
//        val endDatePicker = dialogView.findViewById<DatePicker>(R.id.endDatePicker)
//        val createButton = dialogView.findViewById<Button>(R.id.createButton)
//        val errorMessage = dialogView.findViewById<TextView>(R.id.errorMessage)
//
//        val dialog = AlertDialog.Builder(this)
//            .setView(dialogView)
//            .create()
//
//        // 🔒 Blocco delle date: aggiorna minDate nel DatePicker di fine
//        startDatePicker.setOnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
//            val calendar = java.util.Calendar.getInstance()
//            calendar.set(year, monthOfYear, dayOfMonth)
//            endDatePicker.minDate = calendar.timeInMillis
//        }
//
//        createButton.setOnClickListener {
//            val title = titleEditText.text.toString().trim()
//
//            val startDate = "${startDatePicker.dayOfMonth}/${startDatePicker.month + 1}/${startDatePicker.year}"
//            val endDate = "${endDatePicker.dayOfMonth}/${endDatePicker.month + 1}/${endDatePicker.year}"
//
//            if (title.isEmpty()) {
//                errorMessage.visibility = View.VISIBLE
//            } else {
//                val travelItem = TravelItem(title, "$startDate - $endDate")
//                adapter.addItem(travelItem) // 👈 usa il metodo consigliato
//                dialog.dismiss()
//            }
//        }
//
//        dialog.show()
//    }