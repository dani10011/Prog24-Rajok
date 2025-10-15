package com.example.classmasterpro.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper class for managing authentication data in SharedPreferences
 * Stores JWT token and user information persistently
 */
object AuthPreferences {
    private const val PREFS_NAME = "auth_preferences"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_TOKEN = "token"
    private const val KEY_EMAIL = "email"
    private const val KEY_ROLE_ID = "role_id"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Save authentication data after successful login
     * @param context Application context
     * @param userId User's unique identifier
     * @param token JWT Bearer token
     * @param roleId User's role (1=Admin, 2=Instructor, 3=Student)
     * @param email User's email address (optional)
     */
    fun saveAuthData(context: Context, userId: Int, token: String, roleId: Int, email: String? = null) {
        getPreferences(context).edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_TOKEN, token)
            putInt(KEY_ROLE_ID, roleId)
            email?.let { putString(KEY_EMAIL, it) }
            apply()
        }
    }

    /**
     * Get stored JWT token
     * @param context Application context
     * @return Stored token or null if not logged in
     */
    fun getToken(context: Context): String? {
        return getPreferences(context).getString(KEY_TOKEN, null)
    }

    /**
     * Get stored user ID
     * @param context Application context
     * @return Stored user ID or -1 if not logged in
     */
    fun getUserId(context: Context): Int {
        return getPreferences(context).getInt(KEY_USER_ID, -1)
    }

    /**
     * Get stored email
     * @param context Application context
     * @return Stored email or null
     */
    fun getEmail(context: Context): String? {
        return getPreferences(context).getString(KEY_EMAIL, null)
    }

    /**
     * Get stored role ID
     * @param context Application context
     * @return Stored role ID or -1 if not set
     */
    fun getRoleId(context: Context): Int {
        return getPreferences(context).getInt(KEY_ROLE_ID, -1)
    }

    /**
     * Check if user is logged in
     * @param context Application context
     * @return true if token exists, false otherwise
     */
    fun isLoggedIn(context: Context): Boolean {
        val token = getToken(context)
        return !token.isNullOrEmpty()
    }

    /**
     * Clear all authentication data (logout)
     * @param context Application context
     */
    fun clearAuthData(context: Context) {
        getPreferences(context).edit().clear().apply()
    }
}
