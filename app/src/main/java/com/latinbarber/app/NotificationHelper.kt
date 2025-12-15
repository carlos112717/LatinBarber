package com.latinbarber.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.Calendar

class NotificationHelper(private val context: Context) {

    private val CHANNEL_ID = "citas_channel"
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de Citas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones sobre tus reservas en Barbas Cut´s"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 1. Mostrar notificación inmediata
    fun showConfirmationNotification(barberName: String, time: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle("¡Cita Confirmada!")
            .setContentText("Tu reserva con $barberName a las $time está lista.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    // 2. Programar recordatorio REAL
    fun scheduleReminder(appointmentId: Int, dateString: String, timeString: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Ahora sí va a encontrar la clase ReminderReceiver porque ya tiene su propio archivo
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("message", "Tu corte es hoy a las $timeString. ¡No llegues tarde!")
            putExtra("id", appointmentId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointmentId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            val format = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            val fullDateString = "$dateString $timeString"
            val date = format.parse(fullDateString)

            if (date != null) {
                val appointmentTime = date.time
                // 8 horas en milisegundos
                val eightHoursInMillis = 8 * 60 * 60 * 1000

                val triggerTime = appointmentTime - eightHoursInMillis

                if (triggerTime > System.currentTimeMillis()) {
                    // Usamos set() en lugar de setExact... para evitar problemas de permisos en Android 14 por ahora
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    // 3. Notificación para el ADMIN (Nueva Reserva Recibida)
    fun showNewBookingNotification(customerName: String, time: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💰 ¡Nueva Reserva!")
            .setContentText("$customerName ha reservado para las $time")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    // 4. Notificación de CANCELACIÓN (Para el Admin)
    fun showCancellationNotification(customerName: String, time: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_delete) // Icono de papelera o X
            .setContentTitle("❌ Cita Cancelada")
            .setContentText("$customerName ha cancelado su cita de las $time")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

