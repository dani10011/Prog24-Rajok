package com.example.classmasterpro.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/**
 * Makes a simple API call to test connectivity
 * Replace the URL with your actual backend endpoint
 */
suspend fun makeApiCall(): String = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://jsonplaceholder.typicode.com/todos/1")
        .build()

    try {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            response.body?.string() ?: "Empty response"
        } else {
            throw IOException("HTTP ${response.code}: ${response.message}")
        }
    } catch (e: Exception) {
        throw e
    }
}
