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

    // Esta bandera evita que suenen las notificaciones de las citas que YA existían al abrir la app
    private var isInitialLoad = true

    fun initAdmin(context: Context) {
        notificationHelper = NotificationHelper(context)
        startListening()
        deleteOldAppointments()
    }

    // 1. ESCUCHA GLOBAL DE CAMBIOS
    private fun startListening() {
        firestore.collection("appointments")
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                // Si es la primera carga (al abrir la app), no notificamos nada, solo marcamos como "cargado"
                if (isInitialLoad) {
                    isInitialLoad = false
                    return@addSnapshotListener
                }

                // A partir de aquí, cualquier cambio ES EN VIVO
                for (dc in snapshots.documentChanges) {
                    val appt = dc.document.toObject(Appointment::class.java)

                    // ALGUIEN CANCELÓ
                    if (dc.type == DocumentChange.Type.REMOVED) {
                        notificationHelper?.showCancellationNotification(
                            customerName = appt.customerName,
                            time = "${appt.date} ${appt.time}"
                        )
                    }

                    // ALGUIEN RESERVÓ
                    if (dc.type == DocumentChange.Type.ADDED) {
                        notificationHelper?.showNewBookingNotification(
                            customerName = appt.customerName,
                            time = "${appt.date} ${appt.time}"
                        )
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
                                if ((date.time + threeDaysInMillis) < now) {
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