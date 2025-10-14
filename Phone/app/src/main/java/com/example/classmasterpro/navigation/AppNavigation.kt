package com.example.classmasterpro.navigation

import android.nfc.NfcAdapter
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.classmasterpro.screens.LoginScreen
import com.example.classmasterpro.screens.NFCScannerScreen

/**
 * Sealed class representing app navigation routes
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object NFCScanner : Screen("nfc_scanner")
}

/**
 * Main navigation composable for the app
 * Manages navigation between login and NFC scanner screens
 * Also maintains dark mode state across navigation
 */
@Composable
fun AppNavigation(
    nfcAdapter: NfcAdapter?,
    onShowToast: (String) -> Unit
) {
    val navController = rememberNavController()
    var isDarkMode by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.NFCScanner.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onShowToast = onShowToast,
                isDarkMode = isDarkMode,
                onToggleDarkMode = { isDarkMode = !isDarkMode }
            )
        }

        composable(Screen.NFCScanner.route) {
            NFCScannerScreen(
                nfcAdapter = nfcAdapter,
                onShowToast = onShowToast,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.NFCScanner.route) { inclusive = true }
                    }
                },
                isDarkMode = isDarkMode,
                onToggleDarkMode = { isDarkMode = !isDarkMode }
            )
        }
    }
}
