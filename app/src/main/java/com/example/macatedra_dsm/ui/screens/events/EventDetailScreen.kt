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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.macatedra_dsm.data.remote.AttendeeResponse
import com.example.macatedra_dsm.data.remote.EventResponse
import com.example.macatedra_dsm.data.remote.RatingResponse
import com.example.macatedra_dsm.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class EventDetailTab(
    val title: String
) {
    INFO("Información"),
    ATTENDANCE("Asistencia"),
    RATINGS("Comentarios")
}

@Composable
fun EventDetailScreen(
    token: String,
    eventId: String,
    onBack: () -> Unit,
    onRateEvent: (String) -> Unit,
    onInvalidToken: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(EventDetailTab.INFO) }

    var event by remember { mutableStateOf<EventResponse?>(null) }
    var attendees by remember { mutableStateOf<List<AttendeeResponse>>(emptyList()) }
    var ratings by remember { mutableStateOf<List<RatingResponse>>(emptyList()) }

    var attendanceCount by remember { mutableIntStateOf(0) }
    var ratingAverage by remember { mutableDoubleStateOf(0.0) }
    var ratingCount by remember { mutableIntStateOf(0) }

    var isUserConfirmed by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isCancelling by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }

    var refreshKey by remember { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()
    val eventIsPast = isPastEvent(event?.date)

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0B1736),
            Color(0xFF1B2B48),
            Color(0xFF30415F)
        )
    )

    LaunchedEffect(eventId, refreshKey) {
        try {
            isLoading = true
            errorMessage = null

            val bearerToken = "Bearer $token"

            val eventResponse = RetrofitClient.eventsApi.getEventById(
                token = bearerToken,
                eventId = eventId
            )

            if (eventResponse.isSuccessful) {
                event = eventResponse.body()
            } else if (eventResponse.code() == 401 || eventResponse.code() == 403) {
                onInvalidToken()
                return@LaunchedEffect
            } else {
                errorMessage = "No se pudo cargar el evento."
            }

            val statusResponse = RetrofitClient.attendanceApi.getAttendanceStatus(
                token = bearerToken,
                eventId = eventId
            )

            if (statusResponse.isSuccessful) {
                isUserConfirmed = statusResponse.body()?.confirmed == true
            } else if (statusResponse.code() == 401 || statusResponse.code() == 403) {
                onInvalidToken()
                return@LaunchedEffect
            }

            val countResponse = RetrofitClient.attendanceApi.getAttendanceCount(
                token = bearerToken,
                eventId = eventId
            )

            if (countResponse.isSuccessful) {
                attendanceCount = countResponse.body()?.count ?: 0
            } else if (countResponse.code() == 401 || countResponse.code() == 403) {
                onInvalidToken()
                return@LaunchedEffect
            }

            val attendeesResponse = RetrofitClient.attendanceApi.getEventAttendees(
                token = bearerToken,
                eventId = eventId
            )

            if (attendeesResponse.isSuccessful) {
                attendees = attendeesResponse.body().orEmpty()
            } else if (attendeesResponse.code() == 401 || attendeesResponse.code() == 403) {
                onInvalidToken()
                return@LaunchedEffect
            }

            val ratingsResponse = RetrofitClient.ratingsApi.getRatingsByEvent(
                token = bearerToken,
                eventId = eventId
            )

            if (ratingsResponse.isSuccessful) {
                ratings = ratingsResponse.body().orEmpty()

                val validRatings = ratings
                    .mapNotNull { it.rating }
                    .filter { it in 1..5 }

                ratingCount = validRatings.size
                ratingAverage = if (validRatings.isNotEmpty()) {
                    validRatings.average()
                } else {
                    0.0
                }
            } else if (ratingsResponse.code() == 401 || ratingsResponse.code() == 403) {
                onInvalidToken()
                return@LaunchedEffect
            }
        } catch (e: Exception) {
            errorMessage = "Error cargando detalle del evento: ${e.message}"
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
                        text = "Detalle del evento",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Consulta la información completa",
                        color = Color(0xFFCBD5E1),
                        fontSize = 14.sp
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
                EventDetailTab.entries.forEach { tab ->
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
                    contentPadding = PaddingValues(bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (errorMessage != null) {
                        item {
                            ErrorBox(message = errorMessage.orEmpty())
                        }
                    }

                    when (selectedTab) {
                        EventDetailTab.INFO -> {
                            item {
                                EventMainInfoCard(
                                    event = event,
                                    isUserConfirmed = isUserConfirmed,
                                    onCancelAttendance = {
                                        showCancelDialog = true
                                    }
                                )
                            }
                        }

                        EventDetailTab.ATTENDANCE -> {
                            item {
                                AttendanceSummaryCard(
                                    count = attendanceCount
                                )
                            }

                            if (attendees.isEmpty()) {
                                item {
                                    EmptyAttendeesCard()
                                }
                            } else {
                                items(
                                    items = attendees,
                                    key = { attendee ->
                                        attendee.id ?: attendee.uid ?: attendee.hashCode().toString()
                                    }
                                ) { attendee ->
                                    AttendeeCard(attendee = attendee)
                                }
                            }
                        }

                        EventDetailTab.RATINGS -> {
                            if (!eventIsPast) {
                                item {
                                    RatingsUnavailableCard()
                                }
                            } else {
                                item {
                                    RatingSummaryCard(
                                        average = ratingAverage,
                                        count = ratingCount,
                                        canRate = isUserConfirmed,
                                        onRateClick = {
                                            onRateEvent(eventId)
                                        }
                                    )
                                }

                                if (ratings.isEmpty()) {
                                    item {
                                        EmptyRatingsCard()
                                    }
                                } else {
                                    items(
                                        items = ratings,
                                        key = { rating ->
                                            rating.id ?: rating.uid ?: rating.hashCode().toString()
                                        }
                                    ) { rating ->
                                        RatingCommentCard(rating = rating)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showCancelDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (!isCancelling) {
                        showCancelDialog = false
                    }
                },
                containerColor = Color(0xFF111827),
                shape = RoundedCornerShape(28.dp),
                icon = {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.18f)
                    ) {
                        Box(
                            modifier = Modifier.size(62.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = "Cancelar asistencia",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "¿Estás seguro de que quieres cancelar tu asistencia a este evento?",
                        color = Color(0xFFD1D5DB),
                        fontSize = 15.sp,
                        lineHeight = 21.sp
                    )
                },
                confirmButton = {
                    Button(
                        enabled = !isCancelling,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF59E0B),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF92400E),
                            disabledContentColor = Color(0xFFFEF3C7)
                        ),
                        onClick = {
                            scope.launch {
                                try {
                                    isCancelling = true
                                    errorMessage = null

                                    val response = RetrofitClient.attendanceApi.cancelAttendance(
                                        token = "Bearer $token",
                                        eventId = eventId
                                    )

                                    if (response.isSuccessful) {
                                        showCancelDialog = false
                                        refreshKey++
                                    } else if (response.code() == 401 || response.code() == 403) {
                                        onInvalidToken()
                                    } else {
                                        errorMessage = "No se pudo cancelar la asistencia."
                                        showCancelDialog = false
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error cancelando asistencia: ${e.message}"
                                    showCancelDialog = false
                                } finally {
                                    isCancelling = false
                                }
                            }
                        }
                    ) {
                        if (isCancelling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )

                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                            Text(
                                text = "Cancelando...",
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "Sí, cancelar",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !isCancelling,
                        onClick = {
                            showCancelDialog = false
                        }
                    ) {
                        Text(
                            text = "Volver",
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
private fun EventMainInfoCard(
    event: EventResponse?,
    isUserConfirmed: Boolean,
    onCancelAttendance: () -> Unit
) {
    val dateTimeText = listOfNotNull(
        event?.date?.takeIf { it.isNotBlank() },
        event?.time?.takeIf { it.isNotBlank() }
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
                        modifier = Modifier.size(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = event?.title ?: "Evento sin título",
                        color = Color.White,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 27.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = event?.creatorName ?: "Creador no especificado",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = event?.description ?: "Sin descripción disponible.",
                color = Color(0xFFE2E8F0),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            DetailInfoPill(
                label = "Fecha y hora",
                value = dateTimeText,
                icon = Icons.Default.CalendarMonth
            )

            Spacer(modifier = Modifier.height(10.dp))

            DetailInfoPill(
                label = "Ubicación",
                value = event?.location ?: "Sin ubicación",
                icon = Icons.Default.LocationOn
            )

            Spacer(modifier = Modifier.height(10.dp))

            DetailInfoPill(
                label = "ID del evento",
                value = event?.id ?: event?.eventId ?: "--",
                icon = Icons.Default.CheckCircle
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (isUserConfirmed) {
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
                            text = "Tu asistencia está confirmada.",
                            color = Color(0xFFDCFCE7),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B),
                        contentColor = Color.White
                    ),
                    onClick = onCancelAttendance
                ) {
                    Text(
                        text = "Cancelar mi asistencia",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
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
                            text = "No tienes asistencia confirmada para este evento.",
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceSummaryCard(
    count: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF16A34A).copy(alpha = 0.18f)
            ) {
                Box(
                    modifier = Modifier.size(58.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = Color(0xFF86EFAC),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.padding(horizontal = 10.dp))

            Column {
                Text(
                    text = "$count asistentes",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Personas que confirmaron asistencia",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun AttendeeCard(
    attendee: AttendeeResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.10f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF2563EB).copy(alpha = 0.18f)
            ) {
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF93C5FD),
                        modifier = Modifier.size(25.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.padding(horizontal = 8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = attendee.name ?: "Usuario",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = attendee.updatedAt ?: "Sin fecha de confirmación",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF86EFAC),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun RatingSummaryCard(
    average: Double,
    count: Int,
    canRate: Boolean,
    onRateClick: () -> Unit
) {
    val formattedAverage = String.format(Locale.US, "%.1f", average)

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
                    shape = RoundedCornerShape(20.dp),
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

                Spacer(modifier = Modifier.padding(horizontal = 10.dp))

                Column {
                    Text(
                        text = "$formattedAverage / 5",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "$count calificaciones registradas",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                }
            }

            if (canRate) {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White
                    ),
                    onClick = onRateClick
                ) {
                    Text(
                        text = "Calificar evento",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(14.dp))

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
                            text = "Solo quienes confirmaron asistencia pueden calificar.",
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingCommentCard(
    rating: RatingResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.10f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF2563EB).copy(alpha = 0.18f)
                ) {
                    Box(
                        modifier = Modifier.size(42.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF93C5FD),
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = rating.userName ?: rating.name ?: "Usuario",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = rating.createdAt ?: "Sin fecha",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.18f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.padding(horizontal = 3.dp))

                        Text(
                            text = "${rating.rating ?: 0}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = rating.comment ?: "Sin comentario.",
                color = Color(0xFFE2E8F0),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun RatingsUnavailableCard() {
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
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Comentarios no disponibles",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Los comentarios y calificaciones estarán disponibles cuando el evento haya finalizado.",
                color = Color(0xFFCBD5E1),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun EmptyRatingsCard() {
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
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Sin comentarios",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Todavía no hay comentarios o calificaciones para este evento.",
                color = Color(0xFFCBD5E1),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun DetailInfoPill(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.09f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(19.dp)
            )

            Spacer(modifier = Modifier.padding(horizontal = 6.dp))

            Column {
                Text(
                    text = label,
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun EmptyAttendeesCard() {
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
            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = null,
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Sin asistentes",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Todavía nadie ha confirmado asistencia a este evento.",
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