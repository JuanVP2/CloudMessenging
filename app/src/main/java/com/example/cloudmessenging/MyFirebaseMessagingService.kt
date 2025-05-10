package com.example.cloudmessenging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "MyFirebaseMsgService"
        const val CHANNEL_ID = "fcm_default_channel"
        var lastMessage: String? = null
        var lastToken: String? = null
    }
    
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // Guardar el token para usarlo en la UI
        lastToken = token
        
        // Aquí podrías enviar el token a tu servidor
        // sendRegistrationToServer(token)
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")
        
        // Verificar si el mensaje contiene datos
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        }
        
        // Verificar si el mensaje contiene una notificación
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            lastMessage = it.body
            sendNotification(it.title, it.body)
        }
    }
    
    private fun sendNotification(title: String?, messageBody: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title ?: "FCM Notification")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Desde Android Oreo, los canales de notificación son obligatorios
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Firebase Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        notificationManager.notify(0, notificationBuilder.build())
    }
}