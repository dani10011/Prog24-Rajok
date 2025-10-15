package com.example.classmasterpro.utils

/**
 * Configuration object for API endpoints
 *
 * Base URL: https://72ee88981b0e.ngrok-free.app/
 * Swagger Documentation: https://72ee88981b0e.ngrok-free.app/swagger/index.html
 */
object Config {
    const val BASE_URL = "https://72ee88981b0e.ngrok-free.app"

    object Endpoints {
        const val LOGIN = "$BASE_URL/api/Auth/Login"
        const val REGISTER = "$BASE_URL/api/Auth/Register"
        const val GET_USER_INFO = "$BASE_URL/api/User/GetUserInfo"
        const val GET_PENDING_REQUESTS_BY_ROOM = "$BASE_URL/api/RoomEntryRequest/GetPendingRequestsByRoom"
        const val GET_CURRENT_LECTURE_STATUS = "$BASE_URL/api/Student/GetCurrentLectureStatus"
        const val GET_PENDING_REQUESTS_FOR_ONGOING_LECTURE = "$BASE_URL/api/RoomEntryRequest/GetPendingRequestsForOngoingLecture"
        const val APPROVE_STUDENT_ENTRY = "$BASE_URL/api/RoomEntryRequest/ApproveStudentEntry"
        // Add more endpoints as needed
    }
}
