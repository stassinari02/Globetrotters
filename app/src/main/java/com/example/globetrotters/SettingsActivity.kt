package com.example.globetrotters

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatDelegate
import android.content.Intent
import android.net.Uri

class SettingsActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Recupera i componenti della UI
        val permissionsButton = findViewById<Button>(R.id.permissionsButton)
        val themeSwitch = findViewById<Switch>(R.id.themeSwitch)
        val versionText = findViewById<TextView>(R.id.versionText)
        val exitButton = findViewById<Button>(R.id.exitButton)  // Pulsante "Esci"

        // Mostra la versione dell'app
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        versionText.text = "Versione $versionName"

        // Gestisci la richiesta dei permessi quando si clicca il pulsante
        permissionsButton.setOnClickListener {
            checkPermissions()
        }

        // Gestisci il tema chiaro/scuro
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Imposta tema chiaro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                // Imposta tema scuro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        // Gestisci il clic sul pulsante "Esci"
        exitButton.setOnClickListener {
            // Chiudi l'activity e torna alla MainActivity
            finish()
        }
    }

    // Verifica se i permessi necessari sono concessi
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Se i permessi non sono concessi, chiedili
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            Toast.makeText(this, "Permessi già concessi", Toast.LENGTH_SHORT).show()
        }
    }

    // Gestisci la risposta alla richiesta dei permessi
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // Mostra un avviso che i permessi sono necessari
                Toast.makeText(this, "Permessi necessari per fotocamera e GPS", Toast.LENGTH_LONG).show()

                // Puoi anche indirizzare l'utente alle impostazioni dell'app per concedere i permessi manualmente
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
    }
}
