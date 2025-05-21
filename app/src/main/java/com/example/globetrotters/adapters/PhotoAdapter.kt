package com.example.globetrotters.adapters

import android.app.AlertDialog
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.globetrotters.R
import com.example.globetrotters.database.PhotoEntity

class PhotoAdapter(
    private var photos: List<PhotoEntity>,
    private val onPhotoClick: (Int) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    var onSingleDelete: ((PhotoEntity) -> Unit)? = null

    private var selectionMode = false
    private val selectedItems = mutableSetOf<Int>()
    private var onItemSelected: ((Int, Boolean) -> Unit)? = null

    inner class PhotoViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val card: CardView = v.findViewById(R.id.photoCard)
        val img: ImageView = v.findViewById(R.id.photoItemImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.photo_item, parent, false)
        return PhotoViewHolder(v)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]
        holder.img.setImageURI(Uri.parse(photo.uri))

        // Sfondo blu se selezionata in multi-mode, altrimenti bianco
        val bgColor = if (selectionMode && selectedItems.contains(position))
            R.color.teal_200 else android.R.color.white
        holder.card.setCardBackgroundColor(
            ContextCompat.getColor(holder.card.context, bgColor)
        )

        // Click breve
        holder.img.setOnClickListener {
            if (selectionMode) {
                // toggle selezione in multi-mode
                val nowSelected = !selectedItems.contains(position)
                if (nowSelected) selectedItems.add(position) else selectedItems.remove(position)
                notifyItemChanged(position)
                onItemSelected?.invoke(position, nowSelected)
            } else {
                // fullscreen
                onPhotoClick(position)
            }
        }

        // Long-click: singola delete + evidenziazione
        holder.img.setOnLongClickListener {
            if (!selectionMode) {
                // metti in evidenza temporanea
                holder.card.setCardBackgroundColor(
                    ContextCompat.getColor(holder.card.context, R.color.teal_200)
                )
                AlertDialog.Builder(holder.img.context)
                    .setTitle("Conferma eliminazione")
                    .setMessage("Sicuro di volere eliminare questa foto?")
                    .setPositiveButton("Si") { _, _ ->
                        onSingleDelete?.invoke(photo)
                    }
                    .setNegativeButton("No") { _, _ ->
                        // togli evidenza
                        holder.card.setCardBackgroundColor(
                            ContextCompat.getColor(holder.card.context, android.R.color.white)
                        )
                    }
                    .show()
            }
            true
        }
    }

    override fun getItemCount(): Int = photos.size

    fun updateList(newList: List<PhotoEntity>) {
        photos = newList
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getPhotoUris(): List<String> = photos.map { it.uri }

    fun setSelectionMode(enabled: Boolean, onItemSelected: ((Int, Boolean) -> Unit)?) {
        selectionMode = enabled
        this.onItemSelected = onItemSelected
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<PhotoEntity> =
        selectedItems.mapNotNull { photos.getOrNull(it)}
}