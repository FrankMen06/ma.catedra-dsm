package com.example.macatedra_dsm.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface EventApi {

    @GET("events")
    suspend fun getAllEvents(
        @Header("Authorization") token: String
    ): Response<List<EventResponse>>

    @GET("events/upcoming")
    suspend fun getUpcomingEvents(
        @Header("Authorization") token: String
    ): Response<List<EventResponse>>

    @GET("events/past")
    suspend fun getPastEvents(
        @Header("Authorization") token: String
    ): Response<List<EventResponse>>

    @GET("events/creator/me")
    suspend fun getMyEvents(
        @Header("Authorization") token: String
    ): Response<List<EventResponse>>

    @GET("events/{id}")
    suspend fun getEventById(
        @Header("Authorization") token: String,
        @Path("id") eventId: String
    ): Response<EventResponse>

    @POST("events")
    suspend fun createEvent(
        @Header("Authorization") token: String,
        @Body request: CreateEventRequest
    ): Response<EventResponse>

    @PUT("events/{id}")
    suspend fun updateEvent(
        @Header("Authorization") token: String,
        @Path("id") eventId: String,
        @Body request: UpdateEventRequest
    ): Response<EventResponse>

    @DELETE("events/{id}")
    suspend fun deleteEvent(
        @Header("Authorization") token: String,
        @Path("id") eventId: String
    ): Response<MessageResponse>
}

data class CreateEventRequest(
    val title: String,
    val date: String,
    val time: String,
    val location: String,
    val description: String
)

data class UpdateEventRequest(
    val title: String,
    val date: String,
    val time: String,
    val location: String,
    val description: String
)