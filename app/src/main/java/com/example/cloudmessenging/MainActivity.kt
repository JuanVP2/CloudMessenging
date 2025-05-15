// MainActivity.kt
package com.example.cloudmessenging

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessaging
import com.example.cloudmessenging.ui.theme.CloudMessengingTheme

class MainActivity : ComponentActivity() {
    // 1) Receiver para refrescar UI en primer plano
    private val fcmReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            val t = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_TITLE) ?: ""
            val b = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_BODY)  ?: ""
            _titleState.value = t
            _bodyState.value  = b
        }
    }

    // 2) Estados internos
    private val _titleState = mutableStateOf("")
    private val _bodyState  = mutableStateOf("")

    // 3) Launcher para permiso Android 13+
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("MainActivity","POST_NOTIFICATIONS granted? $granted")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 4) Pedir permiso si es Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // 5) Registrar receptor local
        LocalBroadcastManager.getInstance(this).registerReceiver(
            fcmReceiver,
            IntentFilter(MyFirebaseMessagingService.ACTION_MSG)
        )

        // 6) Obtener token FCM
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Log.d("MI_FCM_TOKEN", token)
            MyFirebaseMessagingService.lastToken = token
        }

        // 7) Si venimos de pulsar una notificación (cold start o reentrada), extraer título y body
        intent.extras?.let { extras ->
            _titleState.value = extras.getString(MyFirebaseMessagingService.EXTRA_TITLE) ?: ""
            _bodyState.value  = extras.getString(MyFirebaseMessagingService.EXTRA_BODY)  ?: ""
        }

        // 8) Montar UI
        setContent {
            CloudMessengingTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    FCMDemoScreen(
                        titleState = _titleState.value,
                        bodyState  = _bodyState.value,
                        tokenState = MyFirebaseMessagingService.lastToken ?: ""
                    )
                }
            }
        }
    }

    // 9) También maneja el caso de “actividad ya viva” (singleTop) cuando pulsamos notificación
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.extras?.let { extras ->
            _titleState.value = extras.getString(MyFirebaseMessagingService.EXTRA_TITLE) ?: ""
            _bodyState.value  = extras.getString(MyFirebaseMessagingService.EXTRA_BODY)  ?: ""
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(fcmReceiver)
        super.onDestroy()
    }
}

@Composable
fun FCMDemoScreen(
    titleState: String,
    bodyState:  String,
    tokenState: String
) {
    val clipboard = LocalClipboardManager.current
    val context   = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Firebase Cloud Messaging Demo",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // — Token FCM —
        Card(Modifier.fillMaxWidth().padding(vertical = 8.dp), shape = RoundedCornerShape(8.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Tu token FCM:", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = tokenState,
                    onValueChange = {},
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    readOnly = true
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        clipboard.setText(AnnotatedString(tokenState))
                        Toast.makeText(context, "Token copiado", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Copiar token")
                }
            }
        }

        // — Último título recibido —
        Card(Modifier.fillMaxWidth().padding(vertical = 8.dp), shape = RoundedCornerShape(8.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Último título recibido:", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (titleState.isBlank()) "—" else titleState,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // — Último mensaje recibido —
        Card(Modifier.fillMaxWidth().padding(vertical = 8.dp), shape = RoundedCornerShape(8.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Último mensaje recibido:", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (bodyState.isBlank()) "No hay mensajes aún" else bodyState,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
