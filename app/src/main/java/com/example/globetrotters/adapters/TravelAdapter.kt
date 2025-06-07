package com.example.globetrotters.adapters

import android.content.Intent
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
import com.example.globetrotters.activity.TravelDetailsActivity
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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.travel_item, parent, false)
        return TravelViewHolder(view)
    }

    // TravelAdapter.kt
    override fun onBindViewHolder(holder: TravelViewHolder, position: Int) {
        val travel = travelList[position]

        // Titolo e date
        holder.titleText.text = travel.title
        holder.dateText.text = travel.dateRange

        // Immagine di copertina (opzionale)
        val imageView = holder.itemView.findViewById<ImageView>(R.id.travelImage)
        if (!travel.photoUri.isNullOrEmpty()) {
            imageView.setImageURI(Uri.parse(travel.photoUri))
        } else {
            imageView.setImageResource(android.R.color.darker_gray)
        }

        // Colore della card in modalità selezione
        val cardBackground = if (selectedItems.contains(position))
            R.color.teal_200 else android.R.color.white
        (holder.itemView as androidx.cardview.widget.CardView)
            .setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, cardBackground))

        holder.itemView.setOnClickListener {
            if (selectionMode) {
                // Toggle selezione
                val wasSelected = selectedItems.contains(position)
                if (wasSelected) selectedItems.remove(position) else selectedItems.add(position)
                notifyItemChanged(position)
                onItemSelected?.invoke(position, !wasSelected)
            } else {
                // APRE TravelDetailsActivity passando ID e titolo
                val ctx = holder.itemView.context
                val intent = Intent(ctx, TravelDetailsActivity::class.java).apply {
                    putExtra("travel_id", travel.id)           // ← ID fondamentale
                    putExtra("travel_title", travel.title)
                }
                ctx.startActivity(intent)
            }
        }

        holder.itemView.setOnLongClickListener {
            // Dialog per cancellazione singola
            val msg = SpannableString("Vuoi eliminare \"${travel.title}\"?")
            val start = msg.indexOf(travel.title)
            msg.setSpan(StyleSpan(Typeface.BOLD), start, start + travel.title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Eliminare vacanza")
                .setMessage(msg)
                .setPositiveButton("Si") { _, _ -> onDeleteRequest(travel) }
                .setNegativeButton("No", null)
                .show()
            true
        }
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

    fun getSelectedItems(): List<TravelEntity> =
        selectedItems.mapNotNull { travelList.getOrNull(it) }
}
