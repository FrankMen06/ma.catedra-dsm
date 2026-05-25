package com.example.macatedra_dsm.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macatedra_dsm.data.remote.ApiErrorResponse
import com.example.macatedra_dsm.data.remote.LoginRequest
import com.example.macatedra_dsm.data.remote.RegisterRequest
import com.example.macatedra_dsm.data.remote.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.launch
import android.util.Log
private val DarkBlue = Color(0xFF0F172A)
private val MidBlue = Color(0xFF1E293B)
private val AccentBlue = Color(0xFF2563EB)
private val AccentGreen = Color(0xFF22C55E)
private val TextSoft = Color(0xFFE2E8F0)
private val TextMuted = Color(0xFFCBD5E1)
private val ErrorRed = Color(0xFFFCA5A5)

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onGoToRegister: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    AuthScreenContainer {
        AuthCard(
            title = "Iniciar sesión",
            subtitle = "Accede a tus eventos, asistencia e historial.",
            footerText = "¿No tienes cuenta?",
            footerActionText = "Crear cuenta",
            onFooterClick = onGoToRegister,
            onBack = onBack
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Correo electrónico") },
                singleLine = true,
                colors = authTextFieldColors()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TextMuted
                        )
                    }
                },
                colors = authTextFieldColors()
            )

            Spacer(modifier = Modifier.height(18.dp))

            AnimatedVisibility(visible = message != null) {
                MessageBox(
                    message = message.orEmpty(),
                    isError = isError
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    scope.launch {

                        if (email.isBlank() || password.isBlank()) {
                            message = "Ingresa correo y contraseña."
                            isError = true
                            return@launch
                        }

                        isLoading = true
                        message = null

                        try {

                            val response = RetrofitClient.authApi.login(
                                LoginRequest(
                                    email = email.trim(),
                                    password = password
                                )
                            )

                            Log.d("LOGIN_DEBUG", "code = ${response.code()}")
                            Log.d("LOGIN_DEBUG", "body = ${response.body()}")
                            Log.d("LOGIN_DEBUG", "error = ${response.errorBody()?.string()}")

                            if (response.isSuccessful) {

                                val body = response.body()
                                val token = body?.token

                                if (!token.isNullOrBlank()) {

                                    message = "Inicio de sesión correcto."
                                    isError = false

                                    onLoginSuccess(token)

                                } else {

                                    message = "El servidor no devolvió token."
                                    isError = true
                                }

                            } else {

                                message = extractErrorMessage(response.errorBody()?.string())
                                isError = true
                            }

                        } catch (e: Exception) {

                            message = "No se pudo conectar con el servidor: ${e.message}"
                            isError = true

                        } finally {

                            isLoading = false
                        }
                    }
                },

                enabled = !isLoading,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),

                shape = RoundedCornerShape(16.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue,
                    contentColor = Color.White,
                    disabledContainerColor = AccentBlue.copy(alpha = 0.45f),
                    disabledContentColor = Color.White.copy(alpha = 0.75f)
                )

            ) {

                if (isLoading) {

                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp
                    )

                } else {

                    Text(
                        text = "Entrar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onGoToLogin: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var photoURL by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    if (showSuccessDialog) {
        SuccessDialog(
            message = successMessage
        )
    }

    AuthScreenContainer {
        AuthCard(
            title = "Crear cuenta",
            subtitle = "Regístrate para participar en eventos académicos.",
            footerText = "¿Ya tienes cuenta?",
            footerActionText = "Iniciar sesión",
            onFooterClick = onGoToLogin,
            onBack = onBack
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre completo") },
                singleLine = true,
                colors = authTextFieldColors()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Correo electrónico") },
                singleLine = true,
                colors = authTextFieldColors()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TextMuted
                        )
                    }
                },
                colors = authTextFieldColors()
            )

            Spacer(modifier = Modifier.height(18.dp))

            AnimatedVisibility(visible = message != null && isError) {
                MessageBox(
                    message = message.orEmpty(),
                    isError = true
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    scope.launch {
                        if (name.isBlank() || email.isBlank() || password.isBlank()) {
                            message = "Completa nombre, correo y contraseña."
                            isError = true
                            return@launch
                        }

                        isLoading = true
                        message = null
                        isError = false

                        try {
                            val response = RetrofitClient.authApi.register(
                                RegisterRequest(
                                    name = name.trim(),
                                    email = email.trim(),
                                    password = password,
                                    photoURL = null
                                )
                            )

                            if (response.isSuccessful) {
                                val body = response.body()

                                successMessage = body?.message ?: "Usuario registrado correctamente. Ahora inicia sesión."
                                showSuccessDialog = true

                                kotlinx.coroutines.delay(2000)

                                showSuccessDialog = false
                                onGoToLogin()
                            } else {
                                message = extractErrorMessage(response.errorBody()?.string())
                                isError = true
                            }
                        } catch (e: Exception) {
                            message = "No se pudo conectar con el servidor: ${e.message}"
                            isError = true
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading && !showSuccessDialog,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreen,
                    contentColor = Color.White,
                    disabledContainerColor = AccentGreen.copy(alpha = 0.45f),
                    disabledContentColor = Color.White.copy(alpha = 0.75f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Registrarme",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthScreenContainer(
    content: @Composable () -> Unit
) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            DarkBlue,
                            MidBlue,
                            Color(0xFF334155)
                        )
                    )
                )
                .padding(innerPadding)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                content()
            }
        }
    }
}

@Composable
private fun AuthCard(
    title: String,
    subtitle: String,
    footerText: String,
    footerActionText: String,
    onFooterClick: () -> Unit,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            TextButton(onClick = onBack) {
                Text(
                    text = "Volver",
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 15.sp,
                lineHeight = 21.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            content()

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = footerText,
                    color = TextMuted,
                    fontSize = 14.sp
                )

                TextButton(onClick = onFooterClick) {
                    Text(
                        text = footerActionText,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBox(
    message: String,
    isError: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isError) ErrorRed else AccentGreen,
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = if (isError) Color(0xFF7F1D1D).copy(alpha = 0.35f)
                else Color(0xFF14532D).copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            text = message,
            color = if (isError) ErrorRed else Color(0xFFBBF7D0),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun SuccessDialog(
    message: String
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = {}
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F172A)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            color = AccentGreen.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = AccentGreen,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Registro exitoso",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = message,
                    color = TextMuted,
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                CircularProgressIndicator(
                    color = AccentGreen,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

@Composable
private fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = TextMuted,
    cursorColor = Color.White,
    focusedBorderColor = AccentBlue,
    unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
    focusedContainerColor = Color.White.copy(alpha = 0.08f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.06f)
)

private fun extractErrorMessage(errorJson: String?): String {
    if (errorJson.isNullOrBlank()) return "Ocurrió un error inesperado."

    return try {
        val error = Gson().fromJson(errorJson, ApiErrorResponse::class.java)
        error.message ?: error.error ?: "Ocurrió un error inesperado."
    } catch (e: Exception) {
        "Ocurrió un error inesperado."
    }
}