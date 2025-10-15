package com.example.classmasterpro.navigation

import android.nfc.NfcAdapter
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.classmasterpro.models.UserRole
import com.example.classmasterpro.screens.InstructorScreen
import com.example.classmasterpro.screens.LoginScreen
import com.example.classmasterpro.screens.NFCScannerScreen
import com.example.classmasterpro.screens.BlackjackScreen
import com.example.classmasterpro.utils.AuthPreferences

/**
 * Sealed class representing app navigation routes
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Instructor : Screen("instructor")
    object NFCScanner : Screen("nfc_scanner")
    object Blackjack : Screen("blackjack")
}

/**
 * Main navigation composable for the app
 * Manages navigation between login and NFC scanner screens
 * Also maintains dark mode state across navigation
 * Checks for existing authentication token to auto-login
 */
@Composable
fun AppNavigation(
    nfcAdapter: NfcAdapter?,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    var isDarkMode by remember { mutableStateOf(false) }

    // Check if user is already logged in and determine start destination based on role
    val isLoggedIn = AuthPreferences.isLoggedIn(context)
    val roleId = AuthPreferences.getRoleId(context)

    val startDestination = if (isLoggedIn) {
        when (roleId) {
            UserRole.INSTRUCTOR.id -> Screen.Instructor.route
            UserRole.STUDENT.id -> Screen.NFCScanner.route
            else -> Screen.Login.route // Invalid role, send to login
        }
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { roleId ->
                    // Route to appropriate screen based on user role
                    val destination = when (roleId) {
                        UserRole.INSTRUCTOR.id -> Screen.Instructor.route
                        UserRole.STUDENT.id -> Screen.NFCScanner.route
                        else -> Screen.Login.route // Should not happen, but failsafe
                    }

                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onShowToast = onShowToast,
                isDarkMode = isDarkMode,
                onToggleDarkMode = { isDarkMode = !isDarkMode }
            )
        }

        composable(Screen.Instructor.route) {
            InstructorScreen(
                onShowToast = onShowToast,
                onLogout = {
                    // Clear authentication data
                    AuthPreferences.clearAuthData(context)
                    // Navigate to login screen
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Instructor.route) { inclusive = true }
                    }
                },
                isDarkMode = isDarkMode,
                onToggleDarkMode = { isDarkMode = !isDarkMode }
            )
        }

        composable(Screen.NFCScanner.route) {
            NFCScannerScreen(
                nfcAdapter = nfcAdapter,
                onShowToast = onShowToast,
                onLogout = {
                    // Clear authentication data
                    AuthPreferences.clearAuthData(context)
                    // Navigate to login screen
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.NFCScanner.route) { inclusive = true }
                    }
                },
                onOpenBlackjack = {
                    navController.navigate(Screen.Blackjack.route)
                },
                isDarkMode = isDarkMode,
                onToggleDarkMode = { isDarkMode = !isDarkMode }
            )
        }

        composable(Screen.Blackjack.route) {
            BlackjackScreen()
        }
    }
}
