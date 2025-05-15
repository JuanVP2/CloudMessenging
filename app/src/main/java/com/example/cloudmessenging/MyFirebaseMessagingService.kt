// MyFirebaseMessagingService.kt
package com.example.cloudmessenging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG       = "MyFirebaseMsgService"
        const val CHANNEL_ID       = "fcm_default_channel"
        const val ACTION_MSG       = "com.example.cloudmessenging.NEW_FCM_MESSAGE"
        const val EXTRA_TITLE      = "extra_title"
        const val EXTRA_BODY       = "extra_body"
        var lastToken: String?     = null
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        lastToken = token
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        val t    = data["title"]
        val b    = data["body"]
        Log.d(TAG, "Payload title=$t  body=$b")

        // 1) Enviar broadcast local para refrescar la UI en primer plano
        Intent(ACTION_MSG).also { broadcast ->
            broadcast.putExtra(EXTRA_TITLE, t)
            broadcast.putExtra(EXTRA_BODY,  b)
            LocalBroadcastManager.getInstance(this)
                .sendBroadcast(broadcast)
        }

        // 2) Construir PendingIntent con extras para la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_TITLE, t)
            putExtra(EXTRA_BODY,  b)
        }
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 3) Mostrar la notificación
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Firebase Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }

        // Construimos el objeto Notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(t ?: "¡Nueva notificación!")
            .setContentText(b ?: "")
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        // Llamada explícita para evitar ambigüedad de overload
        nm.notify(0, notification)
    }
}
