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
 * Expected response format:
 * {
 *   "userId": 2,
 *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "phoneId": "04:12:34:56:78:9A:BC"
 * }
 *
 * @param userId User's unique identifier
 * @param token JWT Bearer token for authenticated requests
 * @param phoneId Phone's unique NFC identifier (7-byte UID in hex format, optional)
 */
data class LoginResponse(
    val userId: Int,
    val token: String,
    val phoneId: String? = null
)

/**
 * User information model from /api/User/GetUserInfo
 * @param userId User's unique identifier
 * @param roleId User's role: 1 = Admin, 2 = Instructor, 3 = Student
 * @param name User's full name
 * @param email User's email address
 * @param neptunCode Student/instructor Neptun code (optional)
 */
data class UserInfo(
    val userId: Int,
    val roleId: Int,
    val name: String? = null,
    val email: String? = null,
    val neptunCode: String? = null
)

/**
 * User roles enum
 */
enum class UserRole(val id: Int) {
    ADMIN(1),
    INSTRUCTOR(2),
    STUDENT(3);

    companion object {
        fun fromId(id: Int): UserRole? = values().find { it.id == id }
    }
}

/**
 * Generic API error response
 */
data class ApiError(
    val message: String,
    val statusCode: Int
)
