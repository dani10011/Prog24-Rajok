package com.example.classmasterpro

import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.classmasterpro.navigation.AppNavigation
import com.example.classmasterpro.ui.theme.ClassMasterProTheme
import com.example.classmasterpro.utils.LanguageHelper

/**
 * Main Activity for ClassMaster Pro
 * An NFC-based class entry system for schools
 */
class MainActivity : ComponentActivity() {
    private lateinit var nfcAdapter: NfcAdapter

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hide system bars (status bar and navigation bar)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.apply {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

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
