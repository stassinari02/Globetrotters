package com.example.globetrotters.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper

// Attività di avvio (splash screen) che viene mostrata brevemente quando l'app viene aperta
class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Usa un Handler per ritardare l'esecuzione del codice nel thread principale
        Handler(Looper.getMainLooper()).postDelayed({

            // Dopo 2 secondi, avvia la MainActivity
            startActivity(Intent(this, MainActivity::class.java))

            // Termina la SplashActivity in modo che non resti nello stack
            finish()

        }, 2000) // Ritardo di 2000 millisecondi (2 secondi)
    }
}
