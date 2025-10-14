package com.example.classmasterpro.utils

import com.example.classmasterpro.models.LoginRequest
import com.example.classmasterpro.models.LoginResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * API Helper for network calls
 * Base URL: https://09cc208360a9.ngrok-free.app/
 */
object ApiHelper {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * Authenticate user with email and password
     * @param email User's email address
     * @param password User's password
     * @return LoginResponse containing authentication token and user info
     * @throws IOException if network request fails
     */
    suspend fun login(email: String, password: String): LoginResponse = withContext(Dispatchers.IO) {
        val loginRequest = LoginRequest(email = email, password = password)
        val requestBody = gson.toJson(loginRequest).toRequestBody(JSON_MEDIA_TYPE)

        val request = Request.Builder()
            .url(Config.Endpoints.LOGIN)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                // Try to parse as LoginResponse
                try {
                    val loginResponse = gson.fromJson(responseBody, LoginResponse::class.java)
                    // If we get a token in the response, consider it successful
                    if (!loginResponse.token.isNullOrEmpty()) {
                        loginResponse.copy(success = true)
                    } else {
                        // Check if response body contains a token string directly
                        if (responseBody.isNotEmpty() && !responseBody.startsWith("{")) {
                            // Response is likely just a token string
                            LoginResponse(token = responseBody, success = true)
                        } else {
                            throw IOException("Login failed: No token received")
                        }
                    }
                } catch (e: Exception) {
                    // If parsing fails, check if response is just a token string
                    if (responseBody.isNotEmpty()) {
                        LoginResponse(token = responseBody, success = true)
                    } else {
                        throw IOException("Login failed: Invalid response format")
                    }
                }
            } else {
                throw IOException("Login failed: HTTP ${response.code} - ${response.message}")
            }
        } catch (e: Exception) {
            throw IOException("Login error: ${e.message}", e)
        }
    }

    /**
     * Makes a simple API call to test connectivity
     * @param token Optional bearer token for authenticated requests
     */
    suspend fun makeApiCall(token: String? = null): String = withContext(Dispatchers.IO) {
        val requestBuilder = Request.Builder()
            .url("${Config.BASE_URL}/faculty")
            .get()

        // Add authorization header if token is provided
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        val request = requestBuilder.build()

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
}
