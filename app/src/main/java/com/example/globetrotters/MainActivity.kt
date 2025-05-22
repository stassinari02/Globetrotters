package com.example.globetrotters

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.globetrotters.adapters.TravelAdapter
import com.example.globetrotters.database.TravelDatabase
import com.example.globetrotters.database.TravelEntity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TravelAdapter
    private var fullTravelList: List<TravelEntity> = emptyList()
    private var deleteModeActive = false
    private var selectedItems = mutableSetOf<Int>()
    private lateinit var db: TravelDatabase
    private val PERMISSIONS_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // RecyclerView setup
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Database & adapter
        db = TravelDatabase.getDatabase(this)
        adapter = TravelAdapter(emptyList()) { travel ->
            // single-delete callback from adapter
            lifecycleScope.launch {
                db.travelDao().deleteTravel(travel)
            }
        }
        recyclerView.adapter = adapter

        // Observe all travels
        db.travelDao().getAllTravels().observe(this, Observer { travels ->
            fullTravelList = travels
            adapter.updateList(travels)
        })

        // Search field
        findViewById<EditText>(R.id.searchEditText).addTextChangedListener { editable ->
            val query = editable.toString().lowercase()
            val filtered = fullTravelList.filter { it.title.lowercase().contains(query) }
            adapter.updateList(filtered)
        }

        // Settings icon
        findViewById<ImageView>(R.id.settingsIcon).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Add new travel button
        findViewById<Button>(R.id.addTravelButton).setOnClickListener {
            startActivity(Intent(this, AddTravelActivity::class.java))
        }

        // Check needed permissions
        checkPermissions()

        // Delete-mode icon & logic
        val deleteIcon: ImageView = findViewById(R.id.deleteIcon)
        val deleteMessage: TextView = findViewById(R.id.deleteModeMessage)
        val superTitle: TextView = findViewById(R.id.superTitleTextView)
        val contentTop = superTitle.parent as LinearLayout

        deleteIcon.setOnClickListener {
            if (!deleteModeActive) {
                // enter multi-delete mode
                deleteModeActive = true
                selectedItems.clear()
                deleteMessage.visibility = View.VISIBLE
                adapter.setSelectionMode(true) { index, isSelected ->
                    if (isSelected) selectedItems.add(index)
                    else selectedItems.remove(index)
                }
                // tap outside to cancel
                contentTop.setOnClickListener { exitDeleteMode() }
            } else {
                // already in delete mode
                if (selectedItems.isNotEmpty()) {
                    // confirm deletion
                    val toDelete = adapter.getSelectedItems()
                    val titles = toDelete.map { it.title }
                    val msgText = "Sicuro di voler eliminare: ${titles.joinToString(", ")}"
                    val spannable = SpannableString(msgText)
                    val start = msgText.indexOf(":") + 2
                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        start, msgText.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    AlertDialog.Builder(this)
                        .setTitle("Conferma eliminazione")
                        .setMessage(spannable)
                        .setPositiveButton("Si") { dialog, _ ->
                            val ids = toDelete.map { it.id }
                            lifecycleScope.launch {
                                db.travelDao().deleteTravelsByIds(ids)
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
                    // nothing selected → toast & exit mode
                    Toast.makeText(this, "Nessun viaggio selezionato", Toast.LENGTH_SHORT).show()
                    exitDeleteMode()
                }
            }
        }
    }

    private fun exitDeleteMode() {
        deleteModeActive = false
        selectedItems.clear()
        findViewById<TextView>(R.id.deleteModeMessage).visibility = View.GONE
        adapter.setSelectionMode(false, null)
        // disable outside-click cancel
        val superTitle: TextView = findViewById(R.id.superTitleTextView)
        (superTitle.parent as LinearLayout).setOnClickListener(null)
    }

    private fun checkPermissions() {
        val perms = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) perms.add(Manifest.permission.CAMERA)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) perms.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
            != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                perms.add(Manifest.permission.READ_MEDIA_IMAGES)
            else
                perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (perms.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, perms.toTypedArray(), PERMISSIONS_REQUEST_CODE)
        }
    }
}
