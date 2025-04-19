package com.example.globetrotters.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.globetrotters.R

class BackArrowFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_back_arrow, container, false)

        val backArrow = view.findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener {
            activity?.finish()
        }

        return view
    }
}