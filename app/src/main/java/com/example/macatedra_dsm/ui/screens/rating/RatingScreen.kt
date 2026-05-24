package com.example.macatedra_dsm.ui.screens.ratings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macatedra_dsm.data.remote.RatingRequest
import com.example.macatedra_dsm.data.remote.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun RatingScreen(
    token: String,
    eventId: String,
    onBack: () -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0B1736),
            Color(0xFF1B2B48),
            Color(0xFF30415F)
        )
    )

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(horizontal = 22.dp, vertical = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        enabled = !loading
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                    Column {
                        Text(
                            text = "Calificar evento",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Comparte tu experiencia del evento",
                            color = Color(0xFFCBD5E1),
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.12f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(22.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.18f)
                            ) {
                                Box(
                                    modifier = Modifier.size(58.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                            Column {
                                Text(
                                    text = "Tu calificación",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Selecciona de 1 a 5 estrellas",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        RatingStarsSelector(
                            rating = rating,
                            onRatingChange = {
                                rating = it
                                errorMessage = null
                            }
                        )

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White.copy(alpha = 0.08f)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Rating seleccionado",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 13.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "$rating / 5",
                                    color = Color.White,
                                    fontSize = 25.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Slider(
                                    value = rating.toFloat(),
                                    onValueChange = {
                                        rating = it.toInt().coerceIn(1, 5)
                                        errorMessage = null
                                    },
                                    valueRange = 1f..5f,
                                    steps = 3,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFFFBBF24),
                                        activeTrackColor = Color(0xFFF59E0B),
                                        inactiveTrackColor = Color.White.copy(alpha = 0.20f),
                                        activeTickColor = Color.White,
                                        inactiveTickColor = Color.White.copy(alpha = 0.35f)
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = comment,
                            onValueChange = {
                                comment = it
                                errorMessage = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 5,
                            shape = RoundedCornerShape(18.dp),
                            label = {
                                Text(text = "Comentario")
                            },
                            placeholder = {
                                Text(text = "Escribe qué te pareció el evento...")
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF60A5FA),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.22f),
                                focusedLabelColor = Color(0xFF93C5FD),
                                unfocusedLabelColor = Color(0xFFCBD5E1),
                                focusedPlaceholderColor = Color(0xFF94A3B8),
                                unfocusedPlaceholderColor = Color(0xFF94A3B8),
                                cursorColor = Color(0xFF60A5FA)
                            )
                        )

                        if (errorMessage != null) {
                            ErrorBox(message = errorMessage.orEmpty())
                        }

                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            enabled = !loading,
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF1E3A8A),
                                disabledContentColor = Color(0xFFBFDBFE)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            onClick = {
                                val cleanComment = comment.trim()

                                if (rating !in 1..5) {
                                    errorMessage = "Selecciona una calificación válida."
                                    return@Button
                                }

                                if (cleanComment.isBlank()) {
                                    errorMessage = "Escribe un comentario para enviar tu calificación."
                                    return@Button
                                }

                                scope.launch {
                                    try {
                                        loading = true
                                        errorMessage = null

                                        val response = RetrofitClient.ratingsApi.createRating(
                                            token = "Bearer $token",
                                            body = RatingRequest(
                                                eventId = eventId,
                                                rating = rating,
                                                comment = cleanComment
                                            )
                                        )

                                        if (response.isSuccessful) {
                                            showSuccessDialog = true
                                        } else {
                                            errorMessage = "No se pudo guardar la calificación. Código: ${response.code()}"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Error enviando calificación: ${e.message}"
                                    } finally {
                                        loading = false
                                    }
                                }
                            }
                        ) {
                            if (loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )

                                Spacer(modifier = Modifier.padding(horizontal = 5.dp))

                                Text(
                                    text = "Enviando...",
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(19.dp)
                                )

                                Spacer(modifier = Modifier.padding(horizontal = 5.dp))

                                Text(
                                    text = "Enviar calificación",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading,
                            onClick = onBack
                        ) {
                            Text(
                                text = "Cancelar",
                                color = Color(0xFFCBD5E1),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }

            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = {},
                    containerColor = Color(0xFF111827),
                    shape = RoundedCornerShape(28.dp),
                    icon = {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Color(0xFF16A34A).copy(alpha = 0.18f)
                        ) {
                            Box(
                                modifier = Modifier.size(62.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF86EFAC),
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                    },
                    title = {
                        Text(
                            text = "Calificación enviada",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = "Tu comentario y calificación se guardaron correctamente.",
                            color = Color(0xFFD1D5DB),
                            fontSize = 15.sp,
                            lineHeight = 21.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF16A34A),
                                contentColor = Color.White
                            ),
                            onClick = {
                                showSuccessDialog = false
                                onBack()
                            }
                        ) {
                            Text(
                                text = "Aceptar",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RatingStarsSelector(
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (star in 1..5) {
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clickable {
                        onRatingChange(star)
                    },
                shape = RoundedCornerShape(16.dp),
                color = if (star <= rating) {
                    Color(0xFFF59E0B).copy(alpha = 0.22f)
                } else {
                    Color.White.copy(alpha = 0.08f)
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating $star",
                        tint = if (star <= rating) {
                            Color(0xFFFBBF24)
                        } else {
                            Color(0xFF94A3B8)
                        },
                        modifier = Modifier.size(29.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorBox(
    message: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF7F1D1D).copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFFCA5A5),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.padding(horizontal = 6.dp))

            Text(
                text = message,
                color = Color(0xFFFCA5A5),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}