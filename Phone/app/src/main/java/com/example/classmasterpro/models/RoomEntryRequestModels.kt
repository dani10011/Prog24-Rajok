package com.example.classmasterpro.models

/**
 * Data models for Room Entry Requests
 */

/**
 * Room Entry Request Response model
 * Response from /api/RoomEntryRequest/GetPendingRequestsByRoom
 */
data class RoomEntryRequestResponse(
    val id: Int,
    val studentId: Int,
    val studentName: String,
    val studentEmail: String,
    val instructorId: Int,
    val instructorName: String,
    val roomId: Int,
    val roomNumber: String,
    val buildingName: String,
    val courseId: Int?,
    val courseName: String?,
    val requestTime: String,  // ISO 8601 datetime string
    val status: String,  // "Pending", "Approved", "Denied", "Expired"
    val reason: String?,
    val responseTime: String?  // ISO 8601 datetime string
)

/**
 * Request model for approving or denying student entry
 * Used with /api/RoomEntryRequest/ApproveStudentEntry
 */
data class ApproveStudentEntryRequest(
    val instructorId: Int,
    val studentId: Int,
    val isApproved: Boolean
)
