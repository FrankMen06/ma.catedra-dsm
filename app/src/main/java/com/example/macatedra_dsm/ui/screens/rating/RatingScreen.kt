package com.example.macatedra_dsm.ui.screens.ratings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.macatedra_dsm.data.remote.RatingRequest
import com.example.macatedra_dsm.data.remote.RetrofitClient
@Composable
fun RatingScreen(
    token: String,
    eventId: String,
    onBack: () -> Unit
) {
    var rating by remember { mutableStateOf(1) }
    var comment by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1736))
            .padding(20.dp)
    ) {

        Text(
            text = "Calificar evento",
            color = Color.White,
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Rating: $rating / 5",
            color = Color.White
        )

        Slider(
            value = rating.toFloat().coerceIn(0f, 5f),
            onValueChange = { rating = it.toInt() },
            valueRange = 0f..5f,
            steps = 4
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Comentario") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                scope.launch {
                    loading = true
                    try {
                        val response = RetrofitClient.ratingsApi.createRating(
                            token = "Bearer $token",
                            body = RatingRequest(
                                eventId = eventId,
                                rating = rating,
                                comment = comment
                            )
                        )

                        if (response.isSuccessful) {
                            println("✅ Rating guardado correctamente")
                            onBack()
                        } else {
                            println("❌ Error: ${response.code()}")
                        }

                    } catch (e: Exception) {
                        println("❌ Exception: ${e.message}")
                    }

                    loading = false
                }
            },
            enabled = rating > 0 && !loading
        ) {
            Text(if (loading) "Enviando..." else "Enviar")
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(onClick = onBack) {
            Text("Cancelar")
        }
    }
}