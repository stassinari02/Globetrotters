package com.example.globetrotters

import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.net.Uri

class SettingsActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Recupera i componenti della UI
        val permissionsButton = findViewById<Button>(R.id.permissionsButton)
        val shareButton = findViewById<Button>(R.id.shareButton)
        val feedbackButton = findViewById<Button>(R.id.feedbackButton)
        val versionText = findViewById<TextView>(R.id.versionText)

        // Mostra la versione dell'app
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        versionText.text = "Versione $versionName"

        // Gestisci il pulsante di condivisione dell'app
        shareButton.setOnClickListener {
            shareApp()
        }

        // Gestisci il pulsante di feedback
        feedbackButton.setOnClickListener {
            openFeedback()
        }

        // Gestisci la richiesta dei permessi
        permissionsButton.setOnClickListener {
            checkPermissions()
        }
    }

    // Funzione per condividere l'app
    private fun shareApp() {
        val appPackageName = packageName // Ottieni il pacchetto dell'app
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Ti consiglio questa app!")
            putExtra(Intent.EXTRA_TEXT, "Dai un'occhiata a questa app fantastica: https://play.google.com/store/apps/details?id=$appPackageName")
        }
        startActivity(Intent.createChooser(shareIntent, "Condividi con"))
    }

    // Funzione per aprire il feedback
    private fun openFeedback() {
        // Mail per supporto
        val feedbackUrl = "mailto:support@globetrotters.com?subject=Feedback%20su%20Globetrotters"
        val feedbackIntent = Intent(Intent.ACTION_SENDTO, Uri.parse(feedbackUrl))
        startActivity(feedbackIntent)
    }

    // Verifica se i permessi necessari sono concessi
    private fun checkPermissions() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    // Gestisci la risposta alla richiesta dei permessi
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // Mostra un avviso che i permessi sono necessari
                Toast.makeText(this, "Permessi necessari per fotocamera, GPS e foto", Toast.LENGTH_LONG).show()

                // Puoi anche indirizzare l'utente alle impostazioni dell'app per concedere i permessi manualmente
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
    }
}
