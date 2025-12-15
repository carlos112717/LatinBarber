package com.latinbarber.app

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("message") ?: "Tienes una cita pendiente"
        val id = intent.getIntExtra("id", 0)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Usamos el mismo canal "citas_channel" que creamos en el Helper
        val notification = NotificationCompat.Builder(context, "citas_channel")
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // Puedes cambiarlo por R.drawable.logo_lb si quieres
            .setContentTitle("⏰ Recordatorio Barbas Cut´s")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}