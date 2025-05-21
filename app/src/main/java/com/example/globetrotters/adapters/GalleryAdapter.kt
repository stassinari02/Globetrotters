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
import com.bumptech.glide.Glide
import com.example.globetrotters.R
import com.example.globetrotters.database.PhotoEntity

class GalleryAdapter(
    private var photos: List<PhotoEntity>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    // callback per cancellazione singola
    var onSingleDelete: ((PhotoEntity) -> Unit)? = null

    // multi-delete
    private val selectedItems = mutableSetOf<Int>()
    private var selectionMode = false
    private var onSelectionChanged: ((Int, Boolean) -> Unit)? = null

    fun setSelectionMode(active: Boolean, callback: ((Int, Boolean) -> Unit)?) {
        selectionMode = active
        onSelectionChanged = callback
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<PhotoEntity> =
        selectedItems.mapNotNull { photos.getOrNull(it) }

    fun getPhotoUris(): Array<String> =
        photos.map { it.uri }.toTypedArray()

    fun updateList(newPhotos: List<PhotoEntity>) {
        photos = newPhotos
        selectedItems.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_photo, parent, false)
        return GalleryViewHolder(v)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val photo = photos[position]
        val uri = Uri.parse(photo.uri)

        // Carica l’immagine con Glide
        Glide.with(holder.itemView.context)
            .load(uri)
            .centerCrop()
            .into(holder.imageView)

        // Background blu (teal_200) se selezionata, altrimenti trasparente
        val context = holder.card.context
        val bgColor = if (selectedItems.contains(position)) {
            ContextCompat.getColor(context, R.color.teal_200)
        } else {
            ContextCompat.getColor(context, android.R.color.transparent)
        }
        holder.card.setCardBackgroundColor(bgColor)

        holder.itemView.setOnClickListener {
            if (selectionMode) {
                toggleSelection(holder, position)
            } else {
                onItemClick(position)
            }
        }

        holder.itemView.setOnLongClickListener {
            if (!selectionMode) {
                // Single‐delete mode
                holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.teal_200))
                AlertDialog.Builder(context)
                    .setTitle("Vuoi eliminare questa foto?")
                    .setPositiveButton("OK") { _, _ ->
                        onSingleDelete?.invoke(photo)
                    }
                    .setNegativeButton("Annulla") { _, _ ->
                        holder.card.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                    }
                    .show()
            } else {
                toggleSelection(holder, position)
            }
            true
        }
    }

    private fun toggleSelection(holder: GalleryViewHolder, pos: Int) {
        val context = holder.card.context
        val now = !selectedItems.contains(pos)
        if (now) selectedItems.add(pos) else selectedItems.remove(pos)
        holder.card.setCardBackgroundColor(
            if (now)
                ContextCompat.getColor(context, R.color.teal_200)
            else
                ContextCompat.getColor(context, android.R.color.transparent)
        )
        onSelectionChanged?.invoke(pos, now)
    }

    override fun getItemCount(): Int = photos.size

    class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: CardView = itemView.findViewById(R.id.galleryCard)
        val imageView: ImageView = itemView.findViewById(R.id.galleryImageView)
    }
}