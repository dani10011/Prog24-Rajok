package com.example.classmasterpro.models

/**
 * Data models for Lecture Status
 */

/**
 * Current Lecture Status Response model
 * Response from /api/Student/GetCurrentLectureStatus
 */
data class CurrentLectureStatusResponse(
    val isInLecture: Boolean,
    val lectureId: Int?,
    val courseName: String?,
    val instructorName: String?,
    val roomNumber: String?,
    val buildingName: String?,
    val startTime: String?,  // ISO 8601 datetime string
    val endTime: String?,    // ISO 8601 datetime string
    val attendanceStatus: String?,  // e.g., "Present", "Absent", "Late"
    val message: String?
)
