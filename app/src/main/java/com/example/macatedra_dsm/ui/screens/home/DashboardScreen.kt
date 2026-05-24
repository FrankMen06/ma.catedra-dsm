package com.example.macatedra_dsm.ui.screens.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.macatedra_dsm.data.remote.EventResponse
import com.example.macatedra_dsm.data.remote.RetrofitClient
import com.example.macatedra_dsm.data.remote.AuthResponse
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    token: String,
    onInvalidToken: () -> Unit,
    onLogout: () -> Unit,
    onGoToEvents: (String) -> Unit
){
    var isLoading by remember { mutableStateOf(true) }
    var user by remember { mutableStateOf<AuthResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var nextEvent by remember { mutableStateOf<EventResponse?>(null) }
    var eventMessage by remember { mutableStateOf<String?>(null) }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var isConfirmingAttendance by remember { mutableStateOf(false) }
    var isLoggingOut by remember { mutableStateOf(false) }

    var refreshKey by remember { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()

    fun logoutFromBackend() {
        if (isLoggingOut) return

        scope.launch {
            try {
                isLoggingOut = true

                RetrofitClient.authApi.logout(
                    token = "Bearer $token"
                )
            } catch (e: Exception) {
                // Aunque falle el logout del backend, cerramos sesión local.
            } finally {
                isLoggingOut = false
                onLogout()
            }
        }
    }

    LaunchedEffect(token, refreshKey) {
        try {
            isLoading = true
            errorMessage = null
            eventMessage = null
            nextEvent = null

            val bearerToken = "Bearer $token"

            val profileResponse = RetrofitClient.authApi.getProfile(bearerToken)

            if (profileResponse.isSuccessful) {
                user = profileResponse.body()
            } else if (profileResponse.code() == 401 || profileResponse.code() == 403) {
                onInvalidToken()
                return@LaunchedEffect
            } else {
                errorMessage = "No se pudo cargar el perfil del usuario."
            }

            val eventsResponse = RetrofitClient.eventsApi.getUpcomingEvents(bearerToken)

            if (eventsResponse.isSuccessful) {
                val events = eventsResponse.body().orEmpty()
                nextEvent = events.firstOrNull()

                if (nextEvent == null) {
                    eventMessage = "No hay eventos próximos para mostrar."
                }
            } else if (eventsResponse.code() == 401 || eventsResponse.code() == 403) {
                onInvalidToken()
                return@LaunchedEffect
            } else {
                eventMessage = "No se pudieron cargar los eventos."
            }
        } catch (e: Exception) {
            errorMessage = "No se pudo cargar la información: ${e.message}"
        } finally {
            isLoading = false
        }
    }

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
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        DashboardHeader(
                            userName = user?.name ?: "Usuario",
                            showMenu = showMenu,
                            isLoggingOut = isLoggingOut,
                            onOpenMenu = { showMenu = true },
                            onDismissMenu = { showMenu = false },
                            onLogout = {
                                showMenu = false
                                logoutFromBackend()
                            },
                            onGoToEvents = { mode ->
                                onGoToEvents(mode)
                            }
                        )
                    }

                    item {
                        SectionTitle(text = "Accesos Rápidos")
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.clickable {
                                    onGoToEvents("ALL")
                                },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color(0xFF2563EB).copy(alpha = 0.18f)
                                ) {
                                    Box(
                                        modifier = Modifier.size(52.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Eventos",
                                            tint = Color(0xFF60A5FA),
                                            modifier = Modifier.size(27.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Eventos",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(
                                modifier = Modifier.clickable {
                                    onGoToEvents("ATTENDING")
                                },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color(0xFF16A34A).copy(alpha = 0.18f)
                                ) {
                                    Box(
                                        modifier = Modifier.size(52.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Asistencia",
                                            tint = Color(0xFF4ADE80),
                                            modifier = Modifier.size(27.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Asistencia",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color(0xFFF59E0B).copy(alpha = 0.18f)
                                ) {
                                    Box(
                                        modifier = Modifier.size(52.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = "Historial",
                                            tint = Color(0xFFFBBF24),
                                            modifier = Modifier.size(27.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Historial",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color(0xFF8B5CF6).copy(alpha = 0.18f)
                                ) {
                                    Box(
                                        modifier = Modifier.size(52.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Forum,
                                            contentDescription = "Social",
                                            tint = Color(0xFFA78BFA),
                                            modifier = Modifier.size(27.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Social",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    item {
                        SectionTitle(text = "Próximo evento")
                    }

                    item {
                        if (nextEvent != null) {
                            NextEventCard(
                                event = nextEvent!!,
                                showConfirmButton = nextEvent?.confirmed != true && !nextEvent?.id.isNullOrBlank(),
                                onConfirmClick = {
                                    showConfirmDialog = true
                                }
                            )
                        } else {
                            EmptyEventCard(
                                message = eventMessage ?: "No hay eventos próximos para mostrar."
                            )
                        }
                    }

                    if (errorMessage != null) {
                        item {
                            ErrorBox(message = errorMessage.orEmpty())
                        }
                    }
                }
            }

            if (showConfirmDialog) {
                AlertDialog(
                    onDismissRequest = {
                        if (!isConfirmingAttendance) {
                            showConfirmDialog = false
                        }
                    },
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
                            text = "Confirmar asistencia",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "¿Estás seguro de que quieres asistir a este evento?",
                                color = Color(0xFFD1D5DB),
                                fontSize = 15.sp,
                                lineHeight = 21.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = nextEvent?.title ?: "Evento seleccionado",
                                color = Color(0xFF86EFAC),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 20.sp
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            enabled = !isConfirmingAttendance,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF16A34A),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF14532D),
                                disabledContentColor = Color(0xFFBBF7D0)
                            ),
                            onClick = {
                                val eventId = nextEvent?.id

                                if (eventId.isNullOrBlank()) {
                                    errorMessage = "No se pudo identificar el evento."
                                    showConfirmDialog = false
                                    return@Button
                                }

                                scope.launch {
                                    try {
                                        isConfirmingAttendance = true

                                        val response = RetrofitClient.attendanceApi.confirmAttendance(
                                            token = "Bearer $token",
                                            eventId = eventId
                                        )

                                        if (response.isSuccessful) {
                                            showConfirmDialog = false
                                            refreshKey++
                                        } else if (response.code() == 401 || response.code() == 403) {
                                            showConfirmDialog = false
                                            onInvalidToken()
                                        } else {
                                            errorMessage = "No se pudo confirmar la asistencia."
                                            showConfirmDialog = false
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Error confirmando asistencia: ${e.message}"
                                        showConfirmDialog = false
                                    } finally {
                                        isConfirmingAttendance = false
                                    }
                                }
                            }
                        ) {
                            if (isConfirmingAttendance) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )

                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                                Text(
                                    text = "Confirmando...",
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "Sí, asistiré",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            enabled = !isConfirmingAttendance,
                            onClick = {
                                showConfirmDialog = false
                            }
                        ) {
                            Text(
                                text = "Cancelar",
                                color = Color(0xFFCBD5E1),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    userName: String,
    showMenu: Boolean,
    isLoggingOut: Boolean,
    onOpenMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onLogout: () -> Unit,
    onGoToEvents: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Bienvenido/a",
                color = Color(0xFFCBD5E1),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = userName,
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp
            )
        }

        Box {
            IconButton(
                onClick = onOpenMenu,
                enabled = !isLoggingOut
            ) {
                if (isLoggingOut) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = Color.White
                    )
                }
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = onDismissMenu,
                modifier = Modifier.background(Color(0xFF1E293B))
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (isLoggingOut) "Cerrando sesión..." else "Cerrar sesión",
                            color = Color.White
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = Color(0xFFFCA5A5)
                        )
                    },
                    enabled = !isLoggingOut,
                    onClick = onLogout
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    text: String
) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun NextEventCard(
    event: EventResponse,
    showConfirmButton: Boolean = false,
    onConfirmClick: () -> Unit = {}
) {
    val dateTimeText = listOfNotNull(
        event.date?.takeIf { it.isNotBlank() },
        event.time?.takeIf { it.isNotBlank() }
    ).joinToString(" - ").ifBlank { "--" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF2563EB).copy(alpha = 0.18f)
                ) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                Column {
                    Text(
                        text = "Evento más cercano",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = event.title ?: "Evento sin título",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 26.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = event.description ?: "Sin descripción disponible.",
                color = Color(0xFFE2E8F0),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            InfoPill(
                modifier = Modifier.fillMaxWidth(),
                label = "Fecha y hora",
                value = dateTimeText
            )

            Spacer(modifier = Modifier.height(10.dp))

            InfoPill(
                modifier = Modifier.fillMaxWidth(),
                label = "Ubicación",
                value = event.location ?: "Sin ubicación"
            )

            if (event.confirmed == true) {
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF16A34A).copy(alpha = 0.18f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF86EFAC),
                            modifier = Modifier.size(21.dp)
                        )

                        Spacer(modifier = Modifier.padding(horizontal = 6.dp))

                        Text(
                            text = "Ya confirmaste tu asistencia a este evento.",
                            color = Color(0xFFDCFCE7),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (showConfirmButton) {
                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        modifier = Modifier.height(42.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF16A34A).copy(alpha = 0.95f),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        onClick = onConfirmClick
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                        Text(
                            text = "Confirmar",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyEventCard(
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.10f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White.copy(alpha = 0.10f)
            ) {
                Box(
                    modifier = Modifier.size(62.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Sin eventos próximos",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                color = Color(0xFFCBD5E1),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun InfoPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.09f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = label,
                color = Color(0xFFCBD5E1),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = value,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ErrorBox(
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF7F1D1D).copy(alpha = 0.35f)
        )
    ) {
        Text(
            text = message,
            color = Color(0xFFFCA5A5),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(14.dp)
        )
    }
}