package com.latinbarber.app

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.latinbarber.app.model.Appointment
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class AdminViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private var notificationHelper: NotificationHelper? = null

    // Esta función se activará al entrar al Panel de Admin
    fun initAdmin(context: Context) {
        notificationHelper = NotificationHelper(context)
        startListening()
        deleteOldAppointments()
    }

    // 1. ESCUCHA GLOBAL DE CAMBIOS (Para Notificaciones)
    private fun startListening() {
        firestore.collection("appointments")
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                for (dc in snapshots.documentChanges) {
                    val appt = dc.document.toObject(Appointment::class.java)

                    // ALGUIEN CANCELÓ (Borraron una cita)
                    if (dc.type == DocumentChange.Type.REMOVED) {
                        notificationHelper?.showCancellationNotification(
                            customerName = appt.customerName,
                            time = "${appt.date} ${appt.time}"
                        )
                    }

                    // ALGUIEN RESERVÓ (Nueva cita reciente)
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val isRecent = (System.currentTimeMillis() - appt.createdAt) < 60000
                        if (isRecent) {
                            notificationHelper?.showNewBookingNotification(
                                customerName = appt.customerName,
                                time = "${appt.date} ${appt.time}"
                            )
                        }
                    }
                }
            }
    }

    // 2. LIMPIEZA AUTOMÁTICA (Borrar citas de hace 3 días)
    private fun deleteOldAppointments() {
        firestore.collection("appointments").get()
            .addOnSuccessListener { result ->
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val now = System.currentTimeMillis()
                val threeDaysInMillis = TimeUnit.DAYS.toMillis(3)

                for (doc in result.documents) {
                    val appt = doc.toObject(Appointment::class.java)
                    if (appt != null) {
                        try {
                            val date = sdf.parse(appt.date)
                            if (date != null) {
                                // Si la cita fue hace más de 3 días (fecha de cita + 3 días < hoy)
                                if ((date.time + threeDaysInMillis) < now) {
                                    // ¡BORRAR!
                                    firestore.collection("appointments").document(doc.id).delete()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
    }
}