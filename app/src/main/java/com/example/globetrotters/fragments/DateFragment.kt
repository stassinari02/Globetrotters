package com.example.globetrotters.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.globetrotters.R
import java.text.SimpleDateFormat
import java.util.*

class DateFragment : Fragment() {

    interface OnDateSelectedListener {
        fun onDatesSelected(start: Calendar, end: Calendar)
    }

    private var listener: OnDateSelectedListener? = null

    private val startDate = Calendar.getInstance()
    private val endDate = Calendar.getInstance()

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        if (context is OnDateSelectedListener) {
            listener = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_date, container, false)

        val startDateText = view.findViewById<TextView>(R.id.startDateText)
        val endDateText = view.findViewById<TextView>(R.id.endDateText)

        // Imposta le date iniziali
        startDateText.text = dateFormat.format(startDate.time)
        endDateText.text = dateFormat.format(endDate.time)

        // Click -> apre il DatePickerDialog
        startDateText.setOnClickListener {
            showDatePicker(startDate) { updated ->
                startDate.time = updated.time
                startDateText.text = dateFormat.format(updated.time)
                notifyDatesSelected()
            }
        }

        endDateText.setOnClickListener {
            showDatePicker(endDate) { updated ->
                endDate.time = updated.time
                endDateText.text = dateFormat.format(updated.time)
                notifyDatesSelected()
            }
        }

        return view
    }

    private fun showDatePicker(initialDate: Calendar, onDateSet: (Calendar) -> Unit) {
        val year = initialDate.get(Calendar.YEAR)
        val month = initialDate.get(Calendar.MONTH)
        val day = initialDate.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, y, m, d ->
            val pickedDate = Calendar.getInstance().apply { set(y, m, d) }
            onDateSet(pickedDate)
        }, year, month, day).show()
    }

    private fun notifyDatesSelected() {
        listener?.onDatesSelected(startDate, endDate)
    }

    fun getStartDate(): Calendar = startDate
    fun getEndDate(): Calendar = endDate
}