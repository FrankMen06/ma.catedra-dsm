package com.example.macatedra_dsm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.macatedra_dsm.data.local.TokenManager
import com.example.macatedra_dsm.ui.screens.auth.LoginScreen
import com.example.macatedra_dsm.ui.screens.auth.RegisterScreen
import com.example.macatedra_dsm.ui.screens.events.CreateEventScreen
import com.example.macatedra_dsm.ui.screens.events.EditEventScreen
import com.example.macatedra_dsm.ui.screens.events.EventsScreen
import com.example.macatedra_dsm.ui.screens.home.DashboardScreen
import com.example.macatedra_dsm.ui.theme.MacatedradsmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MacatedradsmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val navController = rememberNavController()

                    var savedToken by remember {
                        mutableStateOf(TokenManager.getToken(context))
                    }

                    val startDestination = if (savedToken.isNullOrBlank()) {
                        "welcome"
                    } else {
                        "dashboard"
                    }

                    fun clearSessionAndGoWelcome() {
                        TokenManager.clearToken(context)
                        savedToken = null

                        navController.navigate("welcome") {
                            popUpTo("dashboard") {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }

                    fun clearSessionAndGoLogin(fromRoute: String) {
                        TokenManager.clearToken(context)
                        savedToken = null

                        navController.navigate("login") {
                            popUpTo(fromRoute) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable("welcome") {
                            WelcomeDashboardScreen(
                                onGoToLogin = {
                                    navController.navigate("login") {
                                        launchSingleTop = true
                                    }
                                },
                                onGoToRegister = {
                                    navController.navigate("register") {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable("login") {
                            LoginScreen(
                                onBack = {
                                    navController.popBackStack()
                                },
                                onGoToRegister = {
                                    navController.navigate("register") {
                                        launchSingleTop = true
                                    }
                                },
                                onLoginSuccess = { token ->
                                    TokenManager.saveToken(context, token)
                                    savedToken = token

                                    navController.navigate("dashboard") {
                                        popUpTo("welcome") {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable("register") {
                            RegisterScreen(
                                onBack = {
                                    navController.popBackStack()
                                },
                                onGoToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("register") {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable("dashboard") {
                            val token = savedToken

                            if (token.isNullOrBlank()) {
                                LaunchedEffect(Unit) {
                                    navController.navigate("login") {
                                        popUpTo("dashboard") {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            } else {
                                DashboardScreen(
                                    token = token,
                                    onInvalidToken = {
                                        clearSessionAndGoLogin("dashboard")
                                    },
                                    onLogout = {
                                        clearSessionAndGoWelcome()
                                    },
                                    onGoToEvents = {
                                        navController.navigate("events") {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                        }

                        composable("events") {
                            val token = savedToken

                            if (token.isNullOrBlank()) {
                                LaunchedEffect(Unit) {
                                    navController.navigate("login") {
                                        popUpTo("events") {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            } else {
                                EventsScreen(
                                    token = token,
                                    onCreateEvent = {
                                        navController.navigate("create_event") {
                                            launchSingleTop = true
                                        }
                                    },
                                    onEditEvent = { eventId ->
                                        navController.navigate("edit_event/$eventId") {
                                            launchSingleTop = true
                                        }
                                    },
                                    onInvalidToken = {
                                        clearSessionAndGoLogin("events")
                                    }
                                )
                            }
                        }

                        composable("create_event") {
                            val token = savedToken

                            if (token.isNullOrBlank()) {
                                LaunchedEffect(Unit) {
                                    navController.navigate("login") {
                                        popUpTo("create_event") {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            } else {
                                CreateEventScreen(
                                    token = token,
                                    onBack = {
                                        navController.popBackStack()
                                    },
                                    onCreated = {
                                        navController.popBackStack()
                                    },
                                    onInvalidToken = {
                                        clearSessionAndGoLogin("create_event")
                                    }
                                )
                            }
                        }

                        composable("edit_event/{eventId}") { backStackEntry ->
                            val token = savedToken
                            val eventId = backStackEntry.arguments?.getString("eventId").orEmpty()

                            if (token.isNullOrBlank()) {
                                LaunchedEffect(Unit) {
                                    navController.navigate("login") {
                                        popUpTo("edit_event/{eventId}") {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            } else {
                                EditEventScreen(
                                    token = token,
                                    eventId = eventId,
                                    onBack = {
                                        navController.popBackStack()
                                    },
                                    onUpdated = {
                                        navController.popBackStack()
                                    },
                                    onInvalidToken = {
                                        clearSessionAndGoLogin("edit_event/{eventId}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeDashboardScreen(
    onGoToLogin: () -> Unit,
    onGoToRegister: () -> Unit
) {
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF071124),
            Color(0xFF0F1B33),
            Color(0xFF1F3350)
        )
    )

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        color = Color(0xFF2563EB).copy(alpha = 0.12f),
                        shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.BottomStart)
                    .background(
                        color = Color(0xFF60A5FA).copy(alpha = 0.08f),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppLogoBox()

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Cátedra DSM",
                        color = Color.White,
                        fontSize = 35.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 39.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Gestioná eventos académicos, confirmá tu asistencia y participá desde un solo lugar.",
                        color = Color(0xFFD8E4F8),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    WelcomeInfoCard()
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onGoToLogin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Text(
                            text = "Iniciar sesión",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onGoToRegister,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.06f)
                        )
                    ) {
                        Text(
                            text = "Crear cuenta",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    LicenseText()
                }
            }
        }
    }
}

@Composable
fun AppLogoBox() {
    Box(
        modifier = Modifier
            .size(92.dp)
            .shadow(
                elevation = 18.dp,
                shape = RoundedCornerShape(30.dp),
                clip = false
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2563EB),
                        Color(0xFF1D4ED8),
                        Color(0xFF0F172A)
                    )
                ),
                shape = RoundedCornerShape(30.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(30.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "DSM",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun WelcomeInfoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(30.dp),
                clip = false
            ),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.11f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF60A5FA).copy(alpha = 0.16f)
                ) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFF93C5FD),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                Column {
                    Text(
                        text = "Bienvenido",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Tu espacio académico",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Explorá próximos eventos, confirmá tu participación y mantené tu historial organizado de forma sencilla.",
                color = Color(0xFFE2E8F0),
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureItem(
                    modifier = Modifier.weight(1f),
                    title = "Eventos",
                    iconColor = Color(0xFF60A5FA),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                FeatureItem(
                    modifier = Modifier.weight(1f),
                    title = "Asistencia",
                    iconColor = Color(0xFF4ADE80),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4ADE80),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                FeatureItem(
                    modifier = Modifier.weight(1f),
                    title = "Social",
                    iconColor = Color(0xFFA78BFA),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun FeatureItem(
    modifier: Modifier = Modifier,
    title: String,
    iconColor: Color,
    icon: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 13.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = iconColor.copy(alpha = 0.15f)
            ) {
                Box(
                    modifier = Modifier.size(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LicenseText() {
    Text(
        text = "Licencia Creative Commons Atribución 4.0 Internacional",
        color = Color(0xFFCBD5E1).copy(alpha = 0.85f),
        fontSize = 11.sp,
        textAlign = TextAlign.Center,
        lineHeight = 16.sp,
        modifier = Modifier.padding(horizontal = 10.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun WelcomeDashboardPreview() {
    MacatedradsmTheme {
        WelcomeDashboardScreen(
            onGoToLogin = {},
            onGoToRegister = {}
        )
    }
}