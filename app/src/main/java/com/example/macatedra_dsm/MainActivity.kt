package com.example.macatedra_dsm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.macatedra_dsm.data.local.TokenManager
import com.example.macatedra_dsm.ui.screens.auth.LoginScreen
import com.example.macatedra_dsm.ui.screens.auth.RegisterScreen
import com.example.macatedra_dsm.ui.screens.events.AttendingEventsScreen
import com.example.macatedra_dsm.ui.screens.events.CreateEventScreen
import com.example.macatedra_dsm.ui.screens.events.EditEventScreen
import com.example.macatedra_dsm.ui.screens.events.EventDetailScreen
import com.example.macatedra_dsm.ui.screens.events.EventsScreen
import com.example.macatedra_dsm.ui.screens.home.DashboardScreen
import com.example.macatedra_dsm.ui.screens.ratings.EventRatingsScreen
import com.example.macatedra_dsm.ui.screens.ratings.RatingScreen
import com.example.macatedra_dsm.ui.theme.MacatedradsmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MacatedradsmTheme {

                val context = LocalContext.current
                val navController = rememberNavController()

                var savedToken by remember {
                    mutableStateOf(TokenManager.getToken(context))
                }

                val startDestination = if (savedToken.isNullOrBlank()) "login" else "dashboard"

                fun logout() {
                    TokenManager.clearToken(context)
                    savedToken = null

                    navController.navigate("login") {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {

                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = { token ->
                                TokenManager.saveToken(context, token)
                                savedToken = token

                                navController.navigate("dashboard") {
                                    popUpTo("login") {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            },
                            onGoToRegister = {
                                navController.navigate("register") {
                                    launchSingleTop = true
                                }
                            },
                            onBack = {}
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onGoToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("dashboard") {
                        val token = savedToken

                        if (token.isNullOrBlank()) {
                            LaunchedEffect(Unit) {
                                logout()
                            }
                        } else {
                            DashboardScreen(
                                token = token,
                                onLogout = {
                                    logout()
                                },
                                onGoToEvents = { mode ->
                                    when (mode) {
                                        "ATTENDING" -> {
                                            navController.navigate("attending_events") {
                                                launchSingleTop = true
                                            }
                                        }

                                        else -> {
                                            navController.navigate("events") {
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                },
                                onInvalidToken = {
                                    logout()
                                }
                            )
                        }
                    }

                    composable("events") {
                        val token = savedToken

                        if (token.isNullOrBlank()) {
                            LaunchedEffect(Unit) {
                                logout()
                            }
                        } else {
                            EventsScreen(
                                token = token,
                                mode = "ALL",
                                onCreateEvent = {
                                    navController.navigate("create_event") {
                                        launchSingleTop = true
                                    }
                                },
                                onEditEvent = { id ->
                                    navController.navigate("edit_event/$id") {
                                        launchSingleTop = true
                                    }
                                },
                                onRateEvent = { id ->
                                    navController.navigate("rating/$id") {
                                        launchSingleTop = true
                                    }
                                },
                                onViewRatings = { id ->
                                    navController.navigate("ratings/$id") {
                                        launchSingleTop = true
                                    }
                                },
                                onViewEvent = { id ->
                                    navController.navigate("event_detail/$id") {
                                        launchSingleTop = true
                                    }
                                },
                                onInvalidToken = {
                                    logout()
                                }
                            )
                        }
                    }

                    composable("attending_events") {
                        val token = savedToken

                        if (token.isNullOrBlank()) {
                            LaunchedEffect(Unit) {
                                logout()
                            }
                        } else {
                            AttendingEventsScreen(
                                token = token,
                                onBack = {
                                    navController.popBackStack()
                                },
                                onInvalidToken = {
                                    logout()
                                }
                            )
                        }
                    }

                    composable("event_detail/{eventId}") { backStack ->
                        val token = savedToken
                        val id = backStack.arguments?.getString("eventId") ?: ""

                        if (token.isNullOrBlank()) {
                            LaunchedEffect(Unit) {
                                logout()
                            }
                        } else {
                            EventDetailScreen(
                                token = token,
                                eventId = id,
                                onBack = {
                                    navController.popBackStack()
                                },
                                onRateEvent = { eventId ->
                                    navController.navigate("rating/$eventId") {
                                        launchSingleTop = true
                                    }
                                },
                                onInvalidToken = {
                                    logout()
                                }
                            )
                        }
                    }

                    composable("create_event") {
                        val token = savedToken

                        if (token.isNullOrBlank()) {
                            LaunchedEffect(Unit) {
                                logout()
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
                                    logout()
                                }
                            )
                        }
                    }

                    composable("edit_event/{eventId}") { backStack ->
                        val token = savedToken
                        val id = backStack.arguments?.getString("eventId") ?: ""

                        if (token.isNullOrBlank()) {
                            LaunchedEffect(Unit) {
                                logout()
                            }
                        } else {
                            EditEventScreen(
                                token = token,
                                eventId = id,
                                onBack = {
                                    navController.popBackStack()
                                },
                                onUpdated = {
                                    navController.popBackStack()
                                },
                                onInvalidToken = {
                                    logout()
                                }
                            )
                        }
                    }

                    composable("rating/{eventId}") { backStack ->
                        val token = savedToken
                        val id = backStack.arguments?.getString("eventId") ?: ""

                        if (token.isNullOrBlank()) {
                            LaunchedEffect(Unit) {
                                logout()
                            }
                        } else {
                            RatingScreen(
                                token = token,
                                eventId = id,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                    composable("ratings/{eventId}") { backStack ->
                        val token = savedToken
                        val id = backStack.arguments?.getString("eventId") ?: ""

                        if (token.isNullOrBlank()) {
                            LaunchedEffect(Unit) {
                                logout()
                            }
                        } else {
                            EventRatingsScreen(
                                token = token,
                                eventId = id,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}