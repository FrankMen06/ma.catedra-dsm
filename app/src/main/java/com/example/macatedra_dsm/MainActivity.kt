package com.example.macatedra_dsm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.macatedra_dsm.ui.screens.events.EventsScreen
import com.example.macatedra_dsm.ui.screens.home.DashboardScreen
import com.example.macatedra_dsm.ui.theme.MacatedradsmTheme
import com.example.macatedra_dsm.ui.screens.events.CreateEventScreen
import com.example.macatedra_dsm.ui.screens.events.EditEventScreen

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

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable("welcome") {
                            WelcomeDashboardScreen(
                                onGoToLogin = {
                                    navController.navigate("login")
                                },
                                onGoToRegister = {
                                    navController.navigate("register")
                                }
                            )
                        }

                        composable("login") {
                            LoginScreen(
                                onBack = {
                                    navController.popBackStack()
                                },
                                onGoToRegister = {
                                    navController.navigate("register")
                                },
                                onLoginSuccess = { token ->
                                    TokenManager.saveToken(context, token)
                                    savedToken = token

                                    navController.navigate("dashboard") {
                                        popUpTo("welcome") {
                                            inclusive = true
                                        }
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
                                    }
                                }
                            )
                        }

                        composable("dashboard") {
                            val token = savedToken

                            if (token.isNullOrBlank()) {
                                navController.navigate("login") {
                                    popUpTo("dashboard") {
                                        inclusive = true
                                    }
                                }
                            } else {
                                DashboardScreen(
                                    token = token,
                                    onInvalidToken = {
                                        TokenManager.clearToken(context)
                                        savedToken = null

                                        navController.navigate("login") {
                                            popUpTo("dashboard") {
                                                inclusive = true
                                            }
                                        }
                                    },
                                    onLogout = {
                                        TokenManager.clearToken(context)
                                        savedToken = null

                                        navController.navigate("welcome") {
                                            popUpTo("dashboard") {
                                                inclusive = true
                                            }
                                        }
                                    },
                                    onGoToEvents = {
                                        navController.navigate("events")
                                    }
                                )
                            }
                        }

                        composable("events") {
                            val token = savedToken

                            if (token.isNullOrBlank()) {
                                navController.navigate("login") {
                                    popUpTo("events") {
                                        inclusive = true
                                    }
                                }
                            } else {
                                EventsScreen(
                                    token = token,
                                    onCreateEvent = {
                                        navController.navigate("create_event")
                                    },
                                    onEditEvent = { eventId ->
                                        navController.navigate("edit_event/$eventId")
                                    },
                                    onInvalidToken = {
                                        TokenManager.clearToken(context)
                                        savedToken = null

                                        navController.navigate("login") {
                                            popUpTo("events") {
                                                inclusive = true
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        composable("create_event") {
                            val token = savedToken

                            if (token.isNullOrBlank()) {
                                navController.navigate("login") {
                                    popUpTo("create_event") {
                                        inclusive = true
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
                                        TokenManager.clearToken(context)
                                        savedToken = null

                                        navController.navigate("login") {
                                            popUpTo("create_event") {
                                                inclusive = true
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        composable("edit_event/{eventId}") { backStackEntry ->
                            val token = savedToken
                            val eventId = backStackEntry.arguments?.getString("eventId").orEmpty()

                            if (token.isNullOrBlank()) {
                                navController.navigate("login") {
                                    popUpTo("edit_event/{eventId}") {
                                        inclusive = true
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
                                        TokenManager.clearToken(context)
                                        savedToken = null

                                        navController.navigate("login") {
                                            popUpTo("edit_event/{eventId}") {
                                                inclusive = true
                                            }
                                        }
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
fun CreateEventPlaceholderScreen(
    onBack: () -> Unit
) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0B1736),
                            Color(0xFF1B2B48),
                            Color(0xFF30415F)
                        )
                    )
                )
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Crear evento",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Aquí irá la pantalla para crear eventos.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onBack,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Volver",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun EditEventPlaceholderScreen(
    eventId: String,
    onBack: () -> Unit
) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0B1736),
                            Color(0xFF1B2B48),
                            Color(0xFF30415F)
                        )
                    )
                )
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Editar evento",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "ID del evento:",
                    color = Color(0xFFCBD5E1),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = eventId,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onBack,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Volver",
                        fontWeight = FontWeight.SemiBold
                    )
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
    val darkBlue = Color(0xFF0F172A)
    val blue = Color(0xFF2563EB)
    val lightBlue = Color(0xFFDBEAFE)
    val white = Color.White

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            darkBlue,
                            Color(0xFF1E293B),
                            Color(0xFF334155)
                        )
                    )
                )
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppLogoBox()

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Cátedra DSM",
                        color = white,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Sistema de gestión de eventos académicos",
                        color = lightBlue,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

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
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = blue,
                            contentColor = white
                        )
                    ) {
                        Text(
                            text = "Iniciar sesión",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onGoToRegister,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = white,
                            containerColor = Color.Transparent
                        )
                    ) {
                        Text(
                            text = "Crear cuenta",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

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
            .size(96.dp)
            .background(
                color = Color.White.copy(alpha = 0.14f),
                shape = RoundedCornerShape(28.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "DSM",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WelcomeInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Bienvenido",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Explora eventos próximos, confirma tu asistencia, comenta actividades y revisa tu historial de participación.",
                color = Color(0xFFE2E8F0),
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FeatureItem(title = "Eventos")
                FeatureItem(title = "RSVP")
                FeatureItem(title = "Social")
            }
        }
    }
}

@Composable
fun FeatureItem(title: String) {
    Box(
        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 0.10f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LicenseText() {
    Text(
        text = "Licencia: Creative Commons Atribución 4.0 Internacional (CC BY 4.0)",
        color = Color(0xFFCBD5E1),
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        lineHeight = 17.sp,
        modifier = Modifier.padding(horizontal = 8.dp)
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