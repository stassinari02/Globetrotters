package com.example.globetrotters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class FullscreenPagerAdapter(
    private val uris: List<String>
) : RecyclerView.Adapter<FullscreenPagerAdapter.FullscreenViewHolder>() {

    inner class FullscreenViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.fullscreenImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FullscreenViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fullscreen_image, parent, false)
        return FullscreenViewHolder(v)
    }

    override fun onBindViewHolder(holder: FullscreenViewHolder, position: Int) {
        holder.img.setImageURI(Uri.parse(uris[position]))
    }

    override fun getItemCount(): Int = uris.size
}
