package com.example.globetrotters.adapters

import android.content.Context
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.globetrotters.R
import com.example.globetrotters.database.TravelEntity

class TravelAdapter(
    private var travelList: List<TravelEntity>,
    private val onDeleteRequest: (TravelEntity) -> Unit
) : RecyclerView.Adapter<TravelAdapter.TravelViewHolder>() {

    private var selectionMode = false
    private var selectedItems = mutableSetOf<Int>()
    private var onItemSelected: ((Int, Boolean) -> Unit)? = null

    inner class TravelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.travelTitle)
        val dateText: TextView = itemView.findViewById(R.id.travelDates)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.travel_item, parent, false)
        return TravelViewHolder(view)
    }

    override fun onBindViewHolder(holder: TravelViewHolder, position: Int) {
        val travel = travelList[position]
        holder.titleText.text = travel.title
        holder.dateText.text = travel.dateRange

        val imageView = holder.itemView.findViewById<ImageView>(R.id.travelImage)
        if (!travel.photoUri.isNullOrEmpty()) {
            imageView.setImageURI(Uri.parse(travel.photoUri))
        } else {
            imageView.setImageResource(android.R.color.darker_gray)
        }

        val isSelected = selectedItems.contains(position)
        val cardView = holder.itemView as androidx.cardview.widget.CardView
        cardView.setCardBackgroundColor(
            if (isSelected) ContextCompat.getColor(holder.itemView.context, R.color.teal_200)
            else ContextCompat.getColor(holder.itemView.context, R.color.white)
        )

        holder.itemView.setOnClickListener {
            if (selectionMode) {
                val selected = selectedItems.contains(position)
                if (selected) selectedItems.remove(position) else selectedItems.add(position)
                notifyItemChanged(position)
                onItemSelected?.invoke(position, !selected)
            }
        }

        holder.itemView.setOnLongClickListener {
            showDeleteDialog(holder.itemView.context, travel)
            true
        }
    }

    private fun showDeleteDialog(context: Context, travel: TravelEntity) {
        val message = SpannableString("Vuoi eliminare \"${travel.title}\"?")
        val start = message.indexOf(travel.title)
        val end = start + travel.title.length
        message.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        AlertDialog.Builder(context)
            .setTitle("Eliminare vacanza")
            .setMessage(message)
            .setPositiveButton("Si") { dialog, _ ->
                onDeleteRequest(travel)
                dialog.dismiss()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun getItemCount(): Int = travelList.size

    fun updateList(newList: List<TravelEntity>) {
        travelList = newList
        notifyDataSetChanged()
    }

    fun setSelectionMode(enabled: Boolean, onItemSelected: ((Int, Boolean) -> Unit)?) {
        selectionMode = enabled
        this.onItemSelected = onItemSelected
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<TravelEntity> {
        return selectedItems.mapNotNull { position ->
            travelList.getOrNull(position)
        }
    }

}