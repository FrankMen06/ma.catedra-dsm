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
import com.example.macatedra_dsm.ui.screens.events.CreateEventScreen
import com.example.macatedra_dsm.ui.screens.events.EditEventScreen
import com.example.macatedra_dsm.ui.screens.events.EventsScreen
import com.example.macatedra_dsm.ui.screens.home.DashboardScreen
import com.example.macatedra_dsm.ui.theme.MacatedradsmTheme
import com.example.macatedra_dsm.ui.screens.ratings.RatingScreen
import com.example.macatedra_dsm.ui.screens.ratings.EventRatingsScreen
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
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onGoToRegister = {
                                navController.navigate("register")
                            },
                            onBack = {}
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            onBack = { navController.popBackStack() },
                            onGoToLogin = { navController.popBackStack() }
                        )
                    }

                    composable("dashboard") {
                        val token = savedToken

                        if (!token.isNullOrBlank()) {
                            DashboardScreen(
                                token = token,
                                onLogout = { logout() },
                                onGoToEvents = {
                                    navController.navigate("events")
                                },
                                onInvalidToken = { logout() }
                            )
                        }
                    }

                    composable("events") {
                        val token = savedToken

                        if (!token.isNullOrBlank()) {
                            EventsScreen(
                                token = token,
                                mode = "ALL", // <- agregar esto
                                onCreateEvent = {
                                    navController.navigate("create_event")
                                },
                                onEditEvent = { id ->
                                    navController.navigate("edit_event/$id")
                                },
                                onRateEvent = { id ->
                                    navController.navigate("rating/$id")
                                },
                                onViewRatings = { id ->
                                    navController.navigate("ratings/$id")
                                },
                                onInvalidToken = { logout() }
                            )
                        }
                    }

                    composable("create_event") {
                        CreateEventScreen(
                            token = savedToken ?: "",
                            onBack = { navController.popBackStack() },
                            onCreated = { navController.popBackStack() },
                            onInvalidToken = { logout() }
                        )
                    }

                    composable("edit_event/{eventId}") { backStack ->
                        val id = backStack.arguments?.getString("eventId") ?: ""

                        EditEventScreen(
                            token = savedToken ?: "",
                            eventId = id,
                            onBack = { navController.popBackStack() },
                            onUpdated = { navController.popBackStack() },
                            onInvalidToken = { logout() }
                        )
                    }
                    composable("rating/{eventId}") { backStack ->
                        val id = backStack.arguments?.getString("eventId") ?: ""

                        RatingScreen(
                            token = savedToken ?: "",
                            eventId = id,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("ratings/{eventId}") { backStack ->
                        val id = backStack.arguments?.getString("eventId") ?: ""

                        RatingScreen(
                            token = savedToken ?: "",
                            eventId = id,
                            onBack = { navController.popBackStack() }
                        )
                    }
//                    composable("events/{mode}") { backStackEntry ->
//                        val mode = backStackEntry.arguments?.getString("mode") ?: "ALL"
//
//                        EventsScreen(
//                            token = token,
//                            mode = mode,
//                            onCreateEvent = { },
//                            onEditEvent = { },
//                            onInvalidToken = { },
//                            onRateEvent = { },
//                            onViewRatings = { }
//                        )
//                    }
                }
            }
        }
    }
}