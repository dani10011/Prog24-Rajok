package com.example.classmasterpro.models

/**
 * Data models for authentication
 */

/**
 * Login request model
 * @param email User's email address
 * @param password User's password
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Login response model
 * @param token JWT Bearer token for authenticated requests
 * @param email User's email
 * @param role User's role (optional)
 */
data class LoginResponse(
    val token: String? = null,
    val email: String? = null,
    val role: String? = null,
    val message: String? = null,
    val success: Boolean = false
)

/**
 * Generic API error response
 */
data class ApiError(
    val message: String,
    val statusCode: Int
)
