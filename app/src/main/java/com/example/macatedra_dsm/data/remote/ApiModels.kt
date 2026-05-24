package com.example.macatedra_dsm.data.remote

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val photoURL: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val uid: String? = null,
    val name: String? = null,
    val email: String? = null,
    val photoURL: String? = null,
    val provider: String? = null,
    val providerId: String? = null,
    val role: String? = null,
    val token: String? = null,
    val message: String? = null
)

data class ApiErrorResponse(
    val message: String? = null,
    val error: String? = null
)

data class EventResponse(
    val id: String? = null,
    val eventId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val date: String? = null,
    val time: String? = null,
    val location: String? = null,
    val creatorUid: String? = null,
    val creatorName: String? = null,
    val createdAt: String? = null,
    val confirmed: Boolean? = false,
    val attendance: AttendanceData? = null
)

data class AttendanceData(
    val uid: String? = null,
    val name: String? = null,
    val confirmed: Boolean? = false,
    val updatedAt: String? = null
)

data class AttendeeResponse(
    val id: String? = null,
    val uid: String? = null,
    val name: String? = null,
    val confirmed: Boolean? = null,
    val updatedAt: String? = null
)

data class AttendanceCountResponse(
    val count: Int = 0
)

data class CommentResponse(
    val id: String? = null,
    val uid: String? = null,
    val userName: String? = null,
    val comment: String? = null,
    val rating: Int? = null,
    val createdAt: String? = null,
    val editedAt: String? = null
)

data class RatingSummaryResponse(
    val average: Double = 0.0,
    val count: Int = 0
)

data class RatingResponse(
    val id: String? = null,
    val eventId: String? = null,
    val uid: String? = null,
    val userName: String? = null,
    val name: String? = null,
    val rating: Int? = null,
    val comment: String? = null,
    val createdAt: String? = null
)