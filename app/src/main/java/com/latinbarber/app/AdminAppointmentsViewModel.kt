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

    private var notificationHelper: NotificationHelper? = null

    // Variable para evitar notificar las citas que ya existían al abrir la app
    private var isInitialLoad = true

    fun startListening(context: Context) {
        notificationHelper = NotificationHelper(context)
        _isLoading.value = true

        firestore.collection("appointments")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    // 1. SOLUCIÓN AL CRASH: Mapeamos manualmente el ID del documento
                    val list = snapshots.documents.mapNotNull { doc ->
                        val appointment = doc.toObject(Appointment::class.java)
                        // ¡Aquí está la magia! Copiamos el ID real de Firebase al objeto
                        appointment?.copy(id = doc.id)
                    }

                    _appointments.value = list.sortedByDescending { it.createdAt }
                    _isLoading.value = false

                    // 2. LÓGICA DE NOTIFICACIONES MEJORADA
                    if (!isInitialLoad) { // Solo notificamos cambios DESPUÉS de la carga inicial
                        for (dc in snapshots.documentChanges) {
                            val appt = dc.document.toObject(Appointment::class.java)

                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    // Notificamos si es una cita nueva
                                    notificationHelper?.showNewBookingNotification(
                                        customerName = appt.customerName,
                                        time = "${appt.date} ${appt.time}"
                                    )
                                }
                                DocumentChange.Type.REMOVED -> {
                                    // Notificamos cancelación
                                    notificationHelper?.showCancellationNotification(
                                        customerName = appt.customerName,
                                        time = "${appt.date} ${appt.time}"
                                    )
                                }
                                else -> {}
                            }
                        }
                    } else {
                        // Terminó la carga inicial, los siguientes cambios sí son notificaciones reales
                        isInitialLoad = false
                    }
                }
            }
    }
}