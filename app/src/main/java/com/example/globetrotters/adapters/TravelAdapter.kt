package com.example.globetrotters.adapters

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.globetrotters.R
import com.example.globetrotters.models.TravelItem

class TravelAdapter(private val travelList: MutableList<TravelItem>) :
    RecyclerView.Adapter<TravelAdapter.TravelViewHolder>() {

    private var selectionMode = false
    private var selectedPositions = mutableSetOf<Int>()
    private var onItemSelected: ((Int, Boolean) -> Unit)? = null

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

        val imageView = holder.itemView.findViewById<ImageView>(R.id.travelImage)
        if (!travel.photoUri.isNullOrEmpty()) {
            imageView.setImageURI(Uri.parse(travel.photoUri))
        } else {
            imageView.setImageResource(android.R.color.darker_gray) // Placeholder
        }

        // Determiniamo se l'elemento è selezionato
        val isSelected = selectedPositions.contains(position)

        // Impostiamo il colore di sfondo in base alla selezione
        val cardView = holder.itemView as androidx.cardview.widget.CardView
        cardView.setCardBackgroundColor(
            if (isSelected) ContextCompat.getColor(holder.itemView.context, R.color.teal_200)
            else ContextCompat.getColor(holder.itemView.context, R.color.white)
        )


        // Gestire il click per la selezione
        holder.itemView.setOnClickListener {
            if (selectionMode) {
                val selected = selectedPositions.contains(position)
                if (selected) selectedPositions.remove(position) else selectedPositions.add(position)
                notifyItemChanged(position)
                onItemSelected?.invoke(position, !selected)
            }
        }
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

    fun setSelectionMode(enabled: Boolean, onItemSelected: ((Int, Boolean) -> Unit)?) {
        this.selectionMode = enabled
        this.onItemSelected = onItemSelected
        selectedPositions.clear()
        notifyDataSetChanged()
    }

}
