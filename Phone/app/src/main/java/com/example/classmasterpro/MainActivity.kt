package com.example.classmasterpro

import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.classmasterpro.navigation.AppNavigation
import com.example.classmasterpro.ui.theme.ClassMasterProTheme

/**
 * Main Activity for ClassMaster Pro
 * An NFC-based class entry system for schools
 */
class MainActivity : ComponentActivity() {
    private lateinit var nfcAdapter: NfcAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize NFC
        val nfcManager = getSystemService(NFC_SERVICE) as NfcManager
        nfcAdapter = nfcManager.defaultAdapter

        setContent {
            ClassMasterProTheme(dynamicColor = false) {
                AppNavigation(
                    nfcAdapter = nfcAdapter,
                    onShowToast = { message ->
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}
