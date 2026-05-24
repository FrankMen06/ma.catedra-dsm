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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class EventTab(
    val title: String
) {
    ALL("Todos"),
    UPCOMING("Próximos"),
    PAST("Pasados"),
}

@Composable
fun EventsScreen(
    token: String,
    mode: String,
    onCreateEvent: () -> Unit,
    onEditEvent: (String) -> Unit,
    onInvalidToken: () -> Unit,
    onRateEvent: (String) -> Unit,
    onViewRatings: (String) -> Unit,
    onViewEvent: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(EventTab.ALL) }
    var events by remember { mutableStateOf<List<EventResponse>>(emptyList()) }
    var currentUserUid by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(true) }
    var isDeleting by remember { mutableStateOf(false) }
    var isConfirmingAttendance by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var eventToDelete by remember { mutableStateOf<EventResponse?>(null) }
    var eventToConfirm by remember { mutableStateOf<EventResponse?>(null) }

    var refreshKey by remember { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0B1736),
            Color(0xFF1B2B48),
            Color(0xFF30415F)
        )
    )

    LaunchedEffect(selectedTab, refreshKey) {
        try {
            isLoading = true
            errorMessage = null

            val bearerToken = "Bearer $token"

            val profileResponse = RetrofitClient.authApi.getProfile(bearerToken)

            if (profileResponse.isSuccessful) {
                currentUserUid = profileResponse.body()?.uid
            } else if (profileResponse.code() == 401 || profileResponse.code() == 403) {
                onInvalidToken()
                return@LaunchedEffect
            } else {
                errorMessage = "No se pudo cargar la información del usuario."
            }

            val response = when (selectedTab) {
                EventTab.ALL -> RetrofitClient.eventsApi.getAllEvents(bearerToken)
                EventTab.UPCOMING -> RetrofitClient.eventsApi.getUpcomingEvents(bearerToken)
                EventTab.PAST -> RetrofitClient.eventsApi.getPastEvents(bearerToken)
            }

            if (response.isSuccessful) {
                events = response.body().orEmpty()
            } else if (response.code() == 401 || response.code() == 403) {
                onInvalidToken()
            } else {
                events = emptyList()
                errorMessage = "No se pudieron cargar los eventos."
            }
        } catch (e: Exception) {
            events = emptyList()
            errorMessage = "Error cargando eventos: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
            .padding(horizontal = 22.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Eventos",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Administra y consulta actividades",
                        color = Color(0xFFCBD5E1),
                        fontSize = 14.sp
                    )
                }

                FloatingActionButton(
                    modifier = Modifier.size(52.dp),
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    onClick = onCreateEvent
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Crear evento"
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                edgePadding = 0.dp,
                divider = {},
                indicator = {}
            ) {
                EventTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab

                    Tab(
                        selected = isSelected,
                        onClick = {
                            selectedTab = tab
                        },
                        text = {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (isSelected) {
                                    Color.White.copy(alpha = 0.18f)
                                } else {
                                    Color.White.copy(alpha = 0.07f)
                                }
                            ) {
                                Text(
                                    text = tab.title,
                                    color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (events.isEmpty()) {
                        item {
                            EmptyEventsCard(
                                message = when (selectedTab) {
                                    EventTab.ALL -> "No hay eventos registrados."
                                    EventTab.UPCOMING -> "No hay eventos próximos para mostrar."
                                    EventTab.PAST -> "No hay eventos pasados para mostrar."
                                }
                            )
                        }
                    } else {
                        items(
                            items = events,
                            key = { event -> event.id ?: event.eventId ?: event.hashCode().toString() }
                        ) { event ->
                            val eventIsPast = isPastEvent(event.date)
                            val isCreator = !event.creatorUid.isNullOrBlank() &&
                                    event.creatorUid == currentUserUid

                            val canManageEvent = isCreator && !eventIsPast

                            EventCard(
                                event = event,
                                isPastEvent = eventIsPast,
                                isCreator = isCreator,
                                canManageEvent = canManageEvent,
                                showConfirmButton = !eventIsPast &&
                                        event.confirmed != true &&
                                        !event.id.isNullOrBlank(),
                                onConfirmClick = {
                                    eventToConfirm = event
                                },
                                onEditClick = {
                                    val id = event.id ?: event.eventId

                                    if (!id.isNullOrBlank()) {
                                        onEditEvent(id)
                                    }
                                },
                                onDeleteClick = {
                                    eventToDelete = event
                                },
                                onRateClick = {
                                    val id = event.id ?: event.eventId

                                    if (!id.isNullOrBlank()) {
                                        onRateEvent(id)
                                    }
                                },
                                onViewEventClick = {
                                    val id = event.id ?: event.eventId

                                    if (!id.isNullOrBlank()) {
                                        onViewEvent(id)
                                    }
                                }
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
        }

        if (eventToConfirm != null) {
            AlertDialog(
                onDismissRequest = {
                    if (!isConfirmingAttendance) {
                        eventToConfirm = null
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
                            text = eventToConfirm?.title ?: "Evento seleccionado",
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
                            val eventId = eventToConfirm?.id ?: eventToConfirm?.eventId

                            if (eventId.isNullOrBlank()) {
                                errorMessage = "No se pudo identificar el evento."
                                eventToConfirm = null
                                return@Button
                            }

                            if (isPastEvent(eventToConfirm?.date)) {
                                errorMessage = "Este evento ya pasó. Ya no puedes confirmar asistencia."
                                eventToConfirm = null
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
                                        eventToConfirm = null
                                        refreshKey++
                                    } else if (response.code() == 401 || response.code() == 403) {
                                        onInvalidToken()
                                    } else {
                                        errorMessage = "No se pudo confirmar la asistencia."
                                        eventToConfirm = null
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error confirmando asistencia: ${e.message}"
                                    eventToConfirm = null
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
                            eventToConfirm = null
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

        if (eventToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    if (!isDeleting) {
                        eventToDelete = null
                    }
                },
                containerColor = Color(0xFF111827),
                shape = RoundedCornerShape(28.dp),
                icon = {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xFFDC2626).copy(alpha = 0.18f)
                    ) {
                        Box(
                            modifier = Modifier.size(62.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFCA5A5),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = "Eliminar evento",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "¿Estás seguro de que quieres eliminar este evento?",
                            color = Color(0xFFD1D5DB),
                            fontSize = 15.sp,
                            lineHeight = 21.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = eventToDelete?.title ?: "Evento seleccionado",
                            color = Color(0xFFFCA5A5),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 20.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        enabled = !isDeleting,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF7F1D1D),
                            disabledContentColor = Color(0xFFFECACA)
                        ),
                        onClick = {
                            val selectedEvent = eventToDelete

                            if (selectedEvent == null) {
                                errorMessage = "No se pudo identificar el evento."
                                eventToDelete = null
                                return@Button
                            }

                            val eventId = selectedEvent.id ?: selectedEvent.eventId

                            if (eventId.isNullOrBlank()) {
                                errorMessage = "No se pudo identificar el evento."
                                eventToDelete = null
                                return@Button
                            }

                            val selectedEventIsPast = isPastEvent(selectedEvent.date)
                            val selectedEventIsCreator = !selectedEvent.creatorUid.isNullOrBlank() &&
                                    selectedEvent.creatorUid == currentUserUid

                            if (selectedEventIsPast) {
                                errorMessage = "Este evento ya pasó. No se puede eliminar."
                                eventToDelete = null
                                return@Button
                            }

                            if (!selectedEventIsCreator) {
                                errorMessage = "Solo el creador del evento puede eliminarlo."
                                eventToDelete = null
                                return@Button
                            }

                            scope.launch {
                                try {
                                    isDeleting = true

                                    val response = RetrofitClient.eventsApi.deleteEvent(
                                        token = "Bearer $token",
                                        eventId = eventId
                                    )

                                    if (response.isSuccessful) {
                                        eventToDelete = null
                                        refreshKey++
                                    } else if (response.code() == 401 || response.code() == 403) {
                                        onInvalidToken()
                                    } else {
                                        errorMessage = "No se pudo eliminar el evento."
                                        eventToDelete = null
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error eliminando evento: ${e.message}"
                                    eventToDelete = null
                                } finally {
                                    isDeleting = false
                                }
                            }
                        }
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )

                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                            Text(
                                text = "Eliminando...",
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "Sí, eliminar",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !isDeleting,
                        onClick = {
                            eventToDelete = null
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

@Composable
private fun EventCard(
    event: EventResponse,
    isPastEvent: Boolean,
    isCreator: Boolean,
    canManageEvent: Boolean,
    showConfirmButton: Boolean,
    onConfirmClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRateClick: () -> Unit,
    onViewEventClick: () -> Unit
) {
    var showOptionsMenu by remember { mutableStateOf(false) }

    val dateTimeText = listOfNotNull(
        event.date?.takeIf { it.isNotBlank() },
        event.time?.takeIf { it.isNotBlank() }
    ).joinToString(" - ").ifBlank { "--" }

    val canRateEvent = event.confirmed == true && isPastEvent

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
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

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = event.title ?: "Evento sin título",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = event.description ?: "Sin descripción disponible.",
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                Box {
                    IconButton(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.09f),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        onClick = {
                            showOptionsMenu = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones del evento",
                            tint = Color.White,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = {
                            showOptionsMenu = false
                        },
                        modifier = Modifier.background(Color(0xFF1E293B))
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Ver evento",
                                    color = Color.White
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color(0xFF93C5FD)
                                )
                            },
                            onClick = {
                                showOptionsMenu = false
                                onViewEventClick()
                            }
                        )

                        if (canManageEvent) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Editar evento",
                                        color = Color.White
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = Color(0xFFFBBF24)
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    onEditClick()
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Eliminar evento",
                                        color = Color.White
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = Color(0xFFFCA5A5)
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    onDeleteClick()
                                }
                            )
                        }

                        if (canRateEvent) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Calificar evento",
                                        color = Color.White
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFF93C5FD)
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    onRateClick()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.09f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.padding(horizontal = 5.dp))

                    Text(
                        text = dateTimeText,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.09f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.padding(horizontal = 5.dp))

                    Text(
                        text = event.location ?: "Sin ubicación",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (!isCreator && !isPastEvent) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF334155).copy(alpha = 0.45f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.padding(horizontal = 6.dp))

                        Text(
                            text = "Solo el creador puede editar o eliminar este evento.",
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (event.confirmed == true) {
                Spacer(modifier = Modifier.height(12.dp))

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
                            modifier = Modifier.size(20.dp)
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

            if (isPastEvent && event.confirmed != true) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.18f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.padding(horizontal = 6.dp))

                        Text(
                            text = "Este evento ya pasó. Solo quienes confirmaron asistencia pueden calificarlo.",
                            color = Color(0xFFFEF3C7),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (isPastEvent && isCreator) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF7F1D1D).copy(alpha = 0.25f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
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
                            text = "Evento pasado: no se puede editar ni eliminar.",
                            color = Color(0xFFFECACA),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (showConfirmButton) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF16A34A).copy(alpha = 0.95f),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 15.dp),
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
private fun EmptyEventsCard(
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
            modifier = Modifier.padding(24.dp),
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
                        imageVector = Icons.Default.EventBusy,
                        contentDescription = null,
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Sin eventos",
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

private fun isPastEvent(date: String?): Boolean {
    if (date.isNullOrBlank()) return false

    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        formatter.isLenient = false

        val eventDate = formatter.parse(date) ?: return false

        val todayText = formatter.format(Date())
        val todayDate = formatter.parse(todayText) ?: return false

        eventDate.before(todayDate)
    } catch (e: Exception) {
        false
    }
}