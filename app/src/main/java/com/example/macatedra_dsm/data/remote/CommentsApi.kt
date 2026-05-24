package com.example.macatedra_dsm.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface CommentsApi {

    @GET("comments/{eventId}")
    suspend fun getEventComments(
        @Header("Authorization") token: String,
        @Path("eventId") eventId: String
    ): Response<List<CommentResponse>>

    @GET("comments/{eventId}/rating")
    suspend fun getEventRatingSummary(
        @Header("Authorization") token: String,
        @Path("eventId") eventId: String
    ): Response<RatingSummaryResponse>
}