package com.example.classmasterpro.utils

import com.example.classmasterpro.models.LoginRequest
import com.example.classmasterpro.models.LoginResponse
import com.example.classmasterpro.models.UserInfo
import com.example.classmasterpro.models.RoomEntryRequestResponse
import com.example.classmasterpro.models.CurrentLectureStatusResponse
import com.example.classmasterpro.models.ApproveStudentEntryRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
     * @return LoginResponse containing userId, JWT token, and phoneId
     * @throws IOException if network request fails
     *
     * Expected API response:
     * {
     *   "userId": 2,
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "phoneId": "04:12:34:56:78:9A:BC"
     * }
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
            val responseBody = response.body?.string() ?: throw IOException("Empty response from server")

            if (response.isSuccessful) {
                // Parse the JSON response
                try {
                    val loginResponse = gson.fromJson(responseBody, LoginResponse::class.java)
                    if (loginResponse.token.isEmpty()) {
                        throw IOException("Login failed: No token received")
                    }
                    loginResponse
                } catch (e: com.google.gson.JsonSyntaxException) {
                    throw IOException("Login failed: Invalid response format - ${e.message}")
                }
            } else {
                // Try to parse error message from response body
                throw IOException("Login failed: HTTP ${response.code} - ${responseBody.take(100)}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Login error: ${e.message}", e)
        }
    }

    /**
     * Get user information by userId
     * @param userId User's unique identifier
     * @param token JWT Bearer token for authentication
     * @return UserInfo containing user details and role
     * @throws IOException if network request fails
     *
     * Endpoint: GET /api/User/GetUserInfo?userId={userId}
     */
    suspend fun getUserInfo(userId: Int, token: String): UserInfo = withContext(Dispatchers.IO) {
        val url = "${Config.Endpoints.GET_USER_INFO}?userId=$userId"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw IOException("Empty response from server")

            if (response.isSuccessful) {
                try {
                    gson.fromJson(responseBody, UserInfo::class.java)
                } catch (e: com.google.gson.JsonSyntaxException) {
                    throw IOException("Failed to parse user info: ${e.message}")
                }
            } else {
                throw IOException("Get user info failed: HTTP ${response.code} - ${responseBody.take(100)}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Get user info error: ${e.message}", e)
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

    /**
     * Get pending room entry requests for a specific room
     * @param roomId The room ID to get pending requests for
     * @param token JWT Bearer token for authentication
     * @return List of pending room entry requests
     * @throws IOException if network request fails
     *
     * Endpoint: GET /api/RoomEntryRequest/GetPendingRequestsByRoom?roomId={roomId}
     */
    suspend fun getPendingRequestsByRoom(roomId: Int, token: String): List<RoomEntryRequestResponse> = withContext(Dispatchers.IO) {
        val url = "${Config.Endpoints.GET_PENDING_REQUESTS_BY_ROOM}?roomId=$roomId"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw IOException("Empty response from server")

            if (response.isSuccessful) {
                try {
                    val type = object : TypeToken<List<RoomEntryRequestResponse>>() {}.type
                    gson.fromJson(responseBody, type)
                } catch (e: com.google.gson.JsonSyntaxException) {
                    throw IOException("Failed to parse pending requests: ${e.message}")
                }
            } else {
                throw IOException("Get pending requests failed: HTTP ${response.code} - ${responseBody.take(100)}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Get pending requests error: ${e.message}", e)
        }
    }

    /**
     * Get current lecture status for the authenticated student
     * @param userId Student's user ID
     * @param token JWT Bearer token for authentication
     * @return CurrentLectureStatusResponse containing lecture information
     * @throws IOException if network request fails
     *
     * Endpoint: GET /api/Student/GetCurrentLectureStatus/{studentUserId}
     */
    suspend fun getCurrentLectureStatus(userId: Int, token: String): CurrentLectureStatusResponse = withContext(Dispatchers.IO) {
        val url = "${Config.Endpoints.GET_CURRENT_LECTURE_STATUS}/$userId"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw IOException("Empty response from server")

            if (response.isSuccessful) {
                try {
                    gson.fromJson(responseBody, CurrentLectureStatusResponse::class.java)
                } catch (e: com.google.gson.JsonSyntaxException) {
                    throw IOException("Failed to parse lecture status: ${e.message}")
                }
            } else {
                throw IOException("Get lecture status failed: HTTP ${response.code} - ${responseBody.take(100)}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Get lecture status error: ${e.message}", e)
        }
    }

    /**
     * Get pending room entry requests for instructor's ongoing lecture
     * @param instructorId Instructor's user ID
     * @param token JWT Bearer token for authentication
     * @return List of pending room entry requests
     * @throws IOException if network request fails
     *
     * Endpoint: GET /api/RoomEntryRequest/GetPendingRequestsForOngoingLecture?instructorId={instructorId}
     */
    suspend fun getPendingRequestsForOngoingLecture(instructorId: Int, token: String): List<RoomEntryRequestResponse> = withContext(Dispatchers.IO) {
        val url = "${Config.Endpoints.GET_PENDING_REQUESTS_FOR_ONGOING_LECTURE}?instructorId=$instructorId"
        android.util.Log.d("ApiHelper", "Fetching pending requests from: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw IOException("Empty response from server")

            android.util.Log.d("ApiHelper", "Response code: ${response.code}")
            android.util.Log.d("ApiHelper", "Response body: ${responseBody.take(500)}")

            if (response.isSuccessful) {
                try {
                    android.util.Log.d("ApiHelper", "Full response body: $responseBody")
                    val type = object : TypeToken<List<RoomEntryRequestResponse>>() {}.type
                    val result: List<RoomEntryRequestResponse> = gson.fromJson(responseBody, type)
                    android.util.Log.d("ApiHelper", "Successfully parsed ${result.size} requests")
                    result
                } catch (e: com.google.gson.JsonSyntaxException) {
                    android.util.Log.e("ApiHelper", "JSON parsing error: ${e.message}")
                    android.util.Log.e("ApiHelper", "Response body that failed to parse: $responseBody")
                    android.util.Log.e("ApiHelper", "Full exception:", e)
                    throw IOException("Failed to parse pending requests: ${e.message}")
                }
            } else {
                android.util.Log.e("ApiHelper", "HTTP error ${response.code}: $responseBody")
                throw IOException("Get pending requests failed: HTTP ${response.code} - ${responseBody.take(100)}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.e("ApiHelper", "Unexpected error", e)
            throw IOException("Get pending requests error: ${e.message}", e)
        }
    }

    /**
     * Approve or deny student entry for instructor's ongoing lecture
     * @param approvalRequest Request containing instructorId, studentId, and isApproved
     * @param token JWT Bearer token for authentication
     * @return Success response (Unit or success message)
     * @throws IOException if network request fails
     *
     * Endpoint: POST /api/RoomEntryRequest/ApproveStudentEntry
     */
    suspend fun approveStudentEntry(approvalRequest: ApproveStudentEntryRequest, token: String): String = withContext(Dispatchers.IO) {
        val requestBody = gson.toJson(approvalRequest).toRequestBody(JSON_MEDIA_TYPE)

        val request = Request.Builder()
            .url(Config.Endpoints.APPROVE_STUDENT_ENTRY)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw IOException("Empty response from server")

            if (response.isSuccessful) {
                responseBody
            } else {
                throw IOException("Approve student entry failed: HTTP ${response.code} - ${responseBody.take(100)}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Approve student entry error: ${e.message}", e)
        }
    }
}
