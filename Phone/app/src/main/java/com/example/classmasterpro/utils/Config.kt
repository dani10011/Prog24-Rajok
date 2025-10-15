package com.example.classmasterpro.utils

/**
 * Configuration object for API endpoints
 *
 * Base URL: https://09cc208360a9.ngrok-free.app/
 * Swagger Documentation: https://09cc208360a9.ngrok-free.app/swagger/index.html
 */
object Config {
    const val BASE_URL = "https://09cc208360a9.ngrok-free.app"

    object Endpoints {
        const val LOGIN = "$BASE_URL/api/Auth/Login"
        const val REGISTER = "$BASE_URL/api/Auth/Register"
        const val GET_USER_INFO = "$BASE_URL/api/User/GetUserInfo"
        // Add more endpoints as needed
    }
}
