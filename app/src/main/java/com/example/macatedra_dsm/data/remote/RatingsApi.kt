package com.example.macatedra_dsm.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface RatingsApi {

    @POST("/ratings")
    suspend fun createRating(
        @Header("Authorization") token: String,
        @Body body: RatingRequest
    ): Response<Void>
    @GET("/ratings/{eventId}")
    suspend fun getRatingsByEvent(
        @Header("Authorization") token: String,
        @Path("eventId") eventId: String
    ): Response<List<RatingRequest>>
}