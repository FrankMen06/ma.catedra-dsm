package com.example.macatedra_dsm.data.remote

data class RatingRequest(
    val eventId: String,
    val rating: Int,
    val comment: String
)

