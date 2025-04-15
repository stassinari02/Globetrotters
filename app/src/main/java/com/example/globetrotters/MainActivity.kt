package com.example.globetrotters

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import android.text.SpannableString
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.globetrotters.adapters.TravelAdapter
import com.example.globetrotters.models.TravelItem
import android.text.style.StyleSpan
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TravelAdapter
    private val CAMERA_REQUEST_CODE = 101
    private val IMAGE_PICK_REQUEST_CODE = 102
    private val travelList: MutableList<TravelItem> = mutableListOf()
    private val PERMISSIONS_REQUEST_CODE = 100
    private var currentPhotoUri: Uri? = null
    private var photoPreviewImageView: ImageView? = null // 🔥 preview collegata al dialog corrente
    private var deleteModeActive = false
    private var selectedItems = mutableSetOf<Int>()
    private var deleteClickCounter = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        adapter = TravelAdapter(travelList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val addTravelButton: Button = findViewById(R.id.addTravelButton)
        addTravelButton.setOnClickListener {
            showAddTravelDialog()
        }

        val settingsIcon: ImageView = findViewById(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        checkPermissions()

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
                contentTop.setOnClickListener {
                    exitDeleteMode()
                }

            } else {
                // SECONDO CLICK: mostra popup conferma
                if (selectedItems.isNotEmpty()) {
                    // 🔥 Forza il tipo per evitare errore di inferenza
                    val selectedTitles: List<String> = selectedItems.map { index -> travelList[index].title }

                    val message = SpannableString("Sicuro di voler eliminare: ${selectedTitles.joinToString(", ")}")
                    val start = message.indexOf(":") + 2
                    val end = message.length
                    message.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    AlertDialog.Builder(this)
                        .setTitle("Conferma eliminazione")
                        .setMessage(message)
                        .setPositiveButton("Si") { dialog, _ ->
                            val sorted = selectedItems.sortedDescending()
                            for (i in sorted) travelList.removeAt(i)
                            adapter.notifyDataSetChanged()
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
    }

    private fun showAddTravelDialog() {
        currentPhotoUri = null // 👈 Reset del valore quando apri il dialog
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_travel, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.editTitle)
        val startDatePicker = dialogView.findViewById<DatePicker>(R.id.startDatePicker)
        val endDatePicker = dialogView.findViewById<DatePicker>(R.id.endDatePicker)
        val createButton = dialogView.findViewById<Button>(R.id.createButton)
        val errorMessage = dialogView.findViewById<TextView>(R.id.errorMessage)
        val takePhotoButton = dialogView.findViewById<Button>(R.id.takePhotoButton)
        val loadPhotoButton = dialogView.findViewById<Button>(R.id.loadPhotoButton)
        photoPreviewImageView = dialogView.findViewById(R.id.photoPreview)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val backArrow = dialogView.findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener {
            dialog.dismiss()
        }

        takePhotoButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                val photoFile = File.createTempFile("travel_photo_", ".jpg", cacheDir)

                currentPhotoUri = FileProvider.getUriForFile(
                    this,
                    "com.example.globetrotters.provider",
                    photoFile
                )

                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, currentPhotoUri)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            } else {
                Toast.makeText(this, "Permesso fotocamera non concesso", Toast.LENGTH_SHORT).show()
            }
        }

        loadPhotoButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                val intent = Intent(Intent.ACTION_PICK).apply {
                    type = "image/*"
                }
                startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)

            } else {
                Toast.makeText(this, "Permesso lettura immagini non concesso", Toast.LENGTH_SHORT).show()
            }
        }

        createButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val startCalendar = java.util.Calendar.getInstance().apply {
                set(startDatePicker.year, startDatePicker.month, startDatePicker.dayOfMonth)
            }
            val endCalendar = java.util.Calendar.getInstance().apply {
                set(endDatePicker.year, endDatePicker.month, endDatePicker.dayOfMonth)
            }

            // Verifica se la data di fine è precedente alla data di inizio
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
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 1).toInt(),  // 100% della larghezza dello schermo
            LinearLayout.LayoutParams.WRAP_CONTENT)
    }


    // Verifica se i permessi sono in stato "Ask every time"
    private fun shouldRequestPermissions(): Boolean {
        val cameraStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val locationStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val readMediaStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
        val readExternalStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        return ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                cameraStatus == PackageManager.PERMISSION_DENIED ||
                locationStatus == PackageManager.PERMISSION_DENIED ||
                (readMediaStatus == PackageManager.PERMISSION_DENIED && readExternalStatus == PackageManager.PERMISSION_DENIED)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            currentPhotoUri?.let { uri ->
                Toast.makeText(this, "Foto salvata: $uri", Toast.LENGTH_SHORT).show()
                photoPreviewImageView?.setImageURI(uri) // ✅ aggiorna la preview nel dialog visibile
            }
        }
        else if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                currentPhotoUri = uri
                photoPreviewImageView?.setImageURI(uri)
                Toast.makeText(this, "Foto caricata dalla galleria", Toast.LENGTH_SHORT).show()
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
        contentTop.setOnClickListener(null) // disattiva uscita touch
    }
}