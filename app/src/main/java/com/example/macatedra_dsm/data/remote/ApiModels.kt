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
data class AuthLoginResponse(
    val token: String,
    val uid: String?,
    val name: String?,
    val email: String?
)
data class LoginResponse(
    val token: String,
    val uid: String,
    val name: String,
    val email: String
)
data class AuthResponse(
    val uid: String,
    val name: String,
    val email: String,
    val photoURL: String?,
    val provider: String?,
    val providerId: String?,
    val role: String?,
    val createdAt: String?
)

data class ApiErrorResponse(
    val message: String? = null,
    val error: String? = null
)

data class EventResponse(
    val id: String? = null,
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