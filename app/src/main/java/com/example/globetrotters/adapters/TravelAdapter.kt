package com.example.globetrotters.adapters

import android.app.AlertDialog
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.globetrotters.R
import com.example.globetrotters.models.TravelItem

class TravelAdapter(private val travelList: MutableList<TravelItem>) :
    RecyclerView.Adapter<TravelAdapter.TravelViewHolder>() {

    inner class TravelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.travelTitle)
        val dateText: TextView = itemView.findViewById(R.id.travelDates)

        init {
            itemView.setOnLongClickListener {
                showDeleteDialog(adapterPosition, itemView.context)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.travel_item, parent, false)
        return TravelViewHolder(view)
    }

    override fun onBindViewHolder(holder: TravelViewHolder, position: Int) {
        val travel = travelList[position]
        holder.titleText.text = travel.title
        holder.dateText.text = travel.dateRange
    }

    override fun getItemCount(): Int = travelList.size

    fun addItem(item: TravelItem) {
        travelList.add(item)
        notifyItemInserted(travelList.size - 1)
    }

    private fun removeItem(position: Int) {
        if (position in travelList.indices) {
            travelList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private fun showDeleteDialog(position: Int, context: Context) {
        val travel = travelList[position]
        val title = travel.title

        val boldSpan = StyleSpan(Typeface.BOLD)
        val message = SpannableString("Vuoi eliminare \"$title\"?")
        val start = message.indexOf(title)
        val end = start + title.length
        message.setSpan(boldSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        AlertDialog.Builder(context)
            .setTitle("Eliminare vacanza")
            .setMessage(message)
            .setPositiveButton("Si") { dialog, _ ->
                removeItem(position)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
