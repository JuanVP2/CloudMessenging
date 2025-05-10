package com.example.cloudmessenging

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.messaging.FirebaseMessaging
import com.example.cloudmessenging.ui.theme.CloudMessengingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtener token FCM
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Obtener el token y guardarlo
            val token = task.result
            MyFirebaseMessagingService.lastToken = token
            Log.d("FCM", "Token: $token")
        }

        setContent {
            CloudMessengingTheme  {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FCMDemoScreen()
                }
            }
        }
    }
}

@Composable
fun FCMDemoScreen() {
    var lastMessage by remember { mutableStateOf(MyFirebaseMessagingService.lastMessage) }
    var tokenText by remember { mutableStateOf(MyFirebaseMessagingService.lastToken) }

    // Actualizar valores cuando cambian
    LaunchedEffect(Unit) {
        while (true) {
            // Verificar si han cambiado los valores
            if (lastMessage != MyFirebaseMessagingService.lastMessage ||
                tokenText != MyFirebaseMessagingService.lastToken) {
                lastMessage = MyFirebaseMessagingService.lastMessage
                tokenText = MyFirebaseMessagingService.lastToken
            }
            kotlinx.coroutines.delay(1000) // Verificar cada segundo
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Firebase Cloud Messaging Demo",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Tu token FCM:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tokenText ?: "Cargando token...",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Último mensaje recibido:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = lastMessage ?: "No hay mensajes recibidos aún",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
