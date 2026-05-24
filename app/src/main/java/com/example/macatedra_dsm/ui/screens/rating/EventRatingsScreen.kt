package com.example.macatedra_dsm.ui.screens.ratings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macatedra_dsm.data.remote.RatingsApi
import com.example.macatedra_dsm.data.remote.RetrofitClient
import com.example.macatedra_dsm.data.remote.RatingRequest
import com.example.macatedra_dsm.data.remote.RatingResponse
import kotlinx.coroutines.launch

@Composable
fun EventRatingsScreen(
    token: String,
    eventId: String,
    onBack: () -> Unit
) {
    var ratings by remember { mutableStateOf(listOf<RatingResponse>()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    fun loadRatings() {
        scope.launch {
            loading = true
            error = null

            try {
                val response = RetrofitClient.ratingsApi.getRatingsByEvent(
                    token = "Bearer $token",
                    eventId = eventId
                )

                if (response.isSuccessful) {
                    ratings = response.body().orEmpty()
                } else {
                    error = "Error cargando comentarios (${response.code()})"
                }

            } catch (e: Exception) {
                error = "Error: ${e.message}"
            }

            loading = false
        }
    }

    LaunchedEffect(eventId) {
        loadRatings()
    }

    val validRatings = ratings
        .mapNotNull { it.rating }
        .filter { it in 1..5 }

    val average = if (validRatings.isNotEmpty()) {
        validRatings.average()
    } else {
        0.0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1736))
            .padding(16.dp)
    ) {

        Text(
            text = "Comentarios del evento",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Promedio: ${"%.1f".format(average)} ⭐",
            color = Color(0xFFCBD5E1)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onBack) {
            Text("Volver")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Text(
                text = error ?: "",
                color = Color.Red
            )
        } else if (ratings.isEmpty()) {
            Text(
                text = "No hay comentarios aún",
                color = Color(0xFFCBD5E1)
            )
        } else {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ratings) { item ->

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.08f)
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {

                            Text(
                                text = "⭐ ${item.rating}/5",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = item.comment ?: "Sin comentario",
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }
        }
    }
}