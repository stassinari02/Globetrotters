package com.example.globetrotters

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.globetrotters.adapters.TravelAdapter
import com.example.globetrotters.models.TravelItem

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TravelAdapter
    private val travelList = mutableListOf<TravelItem>()

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
    }

    private fun showAddTravelDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_travel, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.editTitle)
        val startDatePicker = dialogView.findViewById<DatePicker>(R.id.startDatePicker)
        val endDatePicker = dialogView.findViewById<DatePicker>(R.id.endDatePicker)
        val createButton = dialogView.findViewById<Button>(R.id.createButton)
        val errorMessage = dialogView.findViewById<TextView>(R.id.errorMessage)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

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

                val travelItem = TravelItem(title, "$startDate - $endDate")
                adapter.addItem(travelItem)
                dialog.dismiss()
            }
        }

        dialog.show()
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