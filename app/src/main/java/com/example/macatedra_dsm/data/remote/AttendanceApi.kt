package com.example.macatedra_dsm.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface AttendanceApi {

    @POST("attendance/{eventId}/confirm")
    suspend fun confirmAttendance(
        @Header("Authorization") token: String,
        @Path("eventId") eventId: String
    ): Response<MessageResponse>

    @POST("attendance/{eventId}/cancel")
    suspend fun cancelAttendance(
        @Header("Authorization") token: String,
        @Path("eventId") eventId: String
    ): Response<MessageResponse>

    @GET("attendance/{eventId}/status")
    suspend fun getAttendanceStatus(
        @Header("Authorization") token: String,
        @Path("eventId") eventId: String
    ): Response<AttendanceStatusResponse>

    @GET("attendance/{eventId}/count")
    suspend fun getAttendanceCount(
        @Header("Authorization") token: String,
        @Path("eventId") eventId: String
    ): Response<AttendanceCountResponse>

    @GET("attendance/{eventId}/attendees")
    suspend fun getEventAttendees(
        @Header("Authorization") token: String,
        @Path("eventId") eventId: String
    ): Response<List<AttendeeResponse>>

    @GET("attendance/my-history")
    suspend fun getMyAttendanceHistory(
        @Header("Authorization") token: String
    ): Response<List<EventResponse>>
}

data class MessageResponse(
    val message: String
)

data class AttendanceStatusResponse(
    val confirmed: Boolean,
    val uid: String? = null,
    val name: String? = null,
    val updatedAt: String? = null
)