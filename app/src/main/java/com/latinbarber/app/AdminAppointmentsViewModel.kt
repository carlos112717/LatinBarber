package com.latinbarber.app

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.latinbarber.app.model.Appointment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminAppointmentsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments = _appointments.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    // Necesitaremos el helper para notificar
    private var notificationHelper: NotificationHelper? = null

    // Esta función la llamaremos desde la pantalla para iniciar la escucha
    fun startListening(context: Context) {
        notificationHelper = NotificationHelper(context)

        _isLoading.value = true

        // ESCUCHA EN TIEMPO REAL
        firestore.collection("appointments")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val list = snapshots.toObjects(Appointment::class.java)
                    _appointments.value = list.sortedByDescending { it.createdAt }
                    _isLoading.value = false

                    // DETECTAR NUEVAS RESERVAS
                    // Recorremos los cambios para ver si se agregó algo nuevo
                    for (dc in snapshots.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val newAppt = dc.document.toObject(Appointment::class.java)
                            // Solo notificamos si la cita se creó hace menos de 1 minuto
                            // (para evitar que suenen todas las viejas al abrir la app)
                            val isRecent = (System.currentTimeMillis() - newAppt.createdAt) < 60000

                            if (isRecent) {
                                notificationHelper?.showNewBookingNotification(
                                    customerName = newAppt.customerName,
                                    time = "${newAppt.date} ${newAppt.time}"
                                )
                            }
                        }
                    }
                }
            }
    }
}