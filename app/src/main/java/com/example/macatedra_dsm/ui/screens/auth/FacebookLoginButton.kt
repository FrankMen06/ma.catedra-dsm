package com.example.macatedra_dsm.ui.screens.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
@Composable
fun FacebookLoginButton(
    onLoginSuccess: (String) -> Unit
) {

    val context = LocalContext.current
    val activity = context.findActivity()

    val callbackManager = remember {
        CallbackManager.Factory.create()
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        callbackManager.onActivityResult(
            64206,
            result.resultCode,
            result.data
        )
    }

    Button(
        onClick = {

            LoginManager.getInstance().registerCallback(
                callbackManager,
                object : FacebookCallback<LoginResult> {

                    override fun onSuccess(result: LoginResult) {

                        val credential = FacebookAuthProvider.getCredential(
                            result.accessToken.token
                        )

                        FirebaseAuth.getInstance()
                            .signInWithCredential(credential)
                            .addOnCompleteListener(activity) { task ->

                                if (task.isSuccessful) {

                                    FirebaseAuth.getInstance()
                                        .currentUser
                                        ?.getIdToken(true)
                                        ?.addOnSuccessListener { tokenResult ->

                                            val firebaseToken = tokenResult.token

                                            if (firebaseToken != null) {
                                                onLoginSuccess(firebaseToken)
                                            }
                                        }
                                }
                            }
                    }

                    override fun onCancel() {
                    }

                    override fun onError(error: FacebookException) {
                        error.printStackTrace()
                    }
                }
            )

            val intent = LoginManager.getInstance()
                .createLogInActivityResultContract(callbackManager)
                .createIntent(
                    activity,
                    listOf("public_profile")
                )

            launcher.launch(intent)
        },

        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),

        shape = RoundedCornerShape(16.dp),

        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1877F2),
            contentColor = Color.White
        )
    ) {
        Text("Continuar con Facebook")
    }
}

fun Context.findActivity(): Activity {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> throw IllegalStateException("No Activity found")
    }
}