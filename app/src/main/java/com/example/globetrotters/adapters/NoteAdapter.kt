package com.example.globetrotters.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.globetrotters.R
import com.example.globetrotters.database.NoteEntity

class NoteAdapter(
    private var notes: List<NoteEntity>
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    var onSingleDelete: ((NoteEntity) -> Unit)? = null

    private var selectionMode = false
    private val selectedItems = mutableSetOf<Int>()
    private var onItemSelected: ((Int, Boolean) -> Unit)? = null

    inner class NoteViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val card: CardView = v.findViewById(R.id.noteCard)
        val text: TextView = v.findViewById(R.id.noteTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_item, parent, false)
        return NoteViewHolder(v)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.text.text = note.text

        // sfondo blu se selezionata in multi-mode, altrimenti bianco
        val bg = if (selectionMode && selectedItems.contains(position))
            R.color.teal_200 else android.R.color.white
        holder.card.setCardBackgroundColor(
            ContextCompat.getColor(holder.card.context, bg)
        )

        // click breve: in multi-mode seleziona/deseleziona, altrimenti nessuna azione
        holder.card.setOnClickListener {
            if (selectionMode) {
                val nowSel = !selectedItems.contains(position)
                if (nowSel) selectedItems.add(position) else selectedItems.remove(position)
                notifyItemChanged(position)
                onItemSelected?.invoke(position, nowSel)
            }
        }

        // long-click: singola cancellazione
        holder.card.setOnLongClickListener {
            if (!selectionMode) {
                holder.card.setCardBackgroundColor(
                    ContextCompat.getColor(holder.card.context, R.color.teal_200)
                )
                AlertDialog.Builder(holder.card.context)
                    .setTitle("Vuoi eliminare questa nota?")
                    .setPositiveButton("OK") { _, _ ->
                        onSingleDelete?.invoke(note)
                    }
                    .setNegativeButton("Annulla") { _, _ ->
                        // togli evidenziazione
                        holder.card.setCardBackgroundColor(
                            ContextCompat.getColor(holder.card.context, android.R.color.white)
                        )
                    }
                    .show()
            }
            true
        }
    }

    override fun getItemCount(): Int = notes.size

    fun updateList(newList: List<NoteEntity>) {
        notes = newList
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun setSelectionMode(enabled: Boolean, onItemSelected: ((Int, Boolean) -> Unit)?) {
        selectionMode = enabled
        this.onItemSelected = onItemSelected
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<NoteEntity> =
        selectedItems.mapNotNull { notes.getOrNull(it) }
}
