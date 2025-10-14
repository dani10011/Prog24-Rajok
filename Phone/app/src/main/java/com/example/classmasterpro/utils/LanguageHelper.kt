package com.example.classmasterpro.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageHelper {
    private const val PREF_NAME = "app_preferences"
    private const val KEY_LANGUAGE = "selected_language"

    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_HUNGARIAN = "hu"

    /**
     * Save the selected language to SharedPreferences
     */
    fun setLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    /**
     * Get the currently selected language
     */
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH
    }

    /**
     * Apply the selected language to the context
     */
    fun applyLanguage(context: Context): Context {
        val languageCode = getLanguage(context)
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    /**
     * Toggle between English and Hungarian
     */
    fun toggleLanguage(context: Context): String {
        val currentLanguage = getLanguage(context)
        val newLanguage = if (currentLanguage == LANGUAGE_ENGLISH) {
            LANGUAGE_HUNGARIAN
        } else {
            LANGUAGE_ENGLISH
        }
        setLanguage(context, newLanguage)
        return newLanguage
    }

    /**
     * Get language display name
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            LANGUAGE_HUNGARIAN -> "Magyar"
            else -> "English"
        }
    }
}
