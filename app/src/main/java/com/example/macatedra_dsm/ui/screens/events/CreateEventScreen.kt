package com.example.macatedra_dsm.ui.screens.events

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.macatedra_dsm.data.remote.CreateEventRequest
import com.example.macatedra_dsm.data.remote.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun CreateEventScreen(
    token: String,
    onBack: () -> Unit,
    onCreated: () -> Unit,
    onInvalidToken: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
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
                        onClick = onBack
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
                            text = "Crear evento",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Registra una nueva actividad",
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
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Color(0xFF2563EB).copy(alpha = 0.18f)
                        ) {
                            Box(
                                modifier = Modifier.padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color(0xFF60A5FA)
                                )
                            }
                        }

                        Text(
                            text = "Información del evento",
                            color = Color.White,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold
                        )

                        AppTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = "Título",
                            placeholder = "Ej. Charla de Desarrollo Móvil"
                        )

                        AppTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = "Fecha",
                            placeholder = "YYYY-MM-DD"
                        )

                        AppTextField(
                            value = time,
                            onValueChange = { time = it },
                            label = "Hora",
                            placeholder = "HH:mm"
                        )

                        AppTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = "Ubicación",
                            placeholder = "Ej. Auditorio principal"
                        )

                        AppTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = "Descripción",
                            placeholder = "Describe brevemente el evento",
                            minLines = 4
                        )

                        if (errorMessage != null) {
                            ErrorBox(message = errorMessage.orEmpty())
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            enabled = !isSaving,
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF1E3A8A),
                                disabledContentColor = Color(0xFFBFDBFE)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            onClick = {
                                val cleanTitle = title.trim()
                                val cleanDate = date.trim()
                                val cleanTime = time.trim()
                                val cleanLocation = location.trim()
                                val cleanDescription = description.trim()

                                if (
                                    cleanTitle.isBlank() ||
                                    cleanDate.isBlank() ||
                                    cleanTime.isBlank() ||
                                    cleanLocation.isBlank() ||
                                    cleanDescription.isBlank()
                                ) {
                                    errorMessage = "Completá todos los campos para crear el evento."
                                    return@Button
                                }

                                scope.launch {
                                    try {
                                        isSaving = true
                                        errorMessage = null

                                        val response = RetrofitClient.eventsApi.createEvent(
                                            token = "Bearer $token",
                                            request = CreateEventRequest(
                                                title = cleanTitle,
                                                date = cleanDate,
                                                time = cleanTime,
                                                location = cleanLocation,
                                                description = cleanDescription
                                            )
                                        )

                                        if (response.isSuccessful) {
                                            showSuccessDialog = true
                                        } else if (response.code() == 401 || response.code() == 403) {
                                            onInvalidToken()
                                        } else {
                                            errorMessage = "No se pudo crear el evento. Revisá los datos e intentá de nuevo."
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Error creando evento: ${e.message}"
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            }
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.padding(end = 10.dp)
                                )

                                Text(
                                    text = "Guardando...",
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null
                                )

                                Spacer(modifier = Modifier.padding(horizontal = 5.dp))

                                Text(
                                    text = "Crear evento",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
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
                                modifier = Modifier.padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    tint = Color(0xFF86EFAC)
                                )
                            }
                        }
                    },
                    title = {
                        Text(
                            text = "Evento creado",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = "El evento se registró correctamente.",
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
                                onCreated()
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
private fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        minLines = minLines,
        shape = RoundedCornerShape(18.dp),
        label = {
            Text(text = label)
        },
        placeholder = {
            Text(text = placeholder)
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
                tint = Color(0xFFFCA5A5)
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