package com.example.globetrotters.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.globetrotters.FullscreenPagerAdapter
import com.example.globetrotters.R

class FullScreenPhotoActivity : AppCompatActivity() {

    // ViewPager per scorrere le foto a schermo intero
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_photo)

        // → Recupera la lista di URI delle foto passate tramite Intent
        val photoUris = intent.getStringArrayExtra("photo_uris")?.toList() ?: emptyList()

        // → Recupera l'indice della foto iniziale da mostrare
        val startIndex = intent.getIntExtra("start_index", 0)

        // → Inizializza il ViewPager2 e imposta l'adapter con le foto
        viewPager = findViewById(R.id.fullscreenViewPager)
        viewPager.adapter = FullscreenPagerAdapter(photoUris)

        // → Imposta la foto iniziale da visualizzare
        viewPager.setCurrentItem(startIndex, false)
    }
}
