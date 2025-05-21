package com.example.globetrotters

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class FullScreenPhotoActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_photo)

        val photoUris = intent.getStringArrayExtra("photo_uris")?.toList() ?: emptyList()
        val startIndex = intent.getIntExtra("start_index", 0)

        viewPager = findViewById(R.id.fullscreenViewPager)
        viewPager.adapter = FullscreenPagerAdapter(photoUris)
        viewPager.setCurrentItem(startIndex, false)
        }
}