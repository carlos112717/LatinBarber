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

    // 👇 ESTA VARIABLE ES LA QUE TE FALTABA PARA QUE FUNCIONE EL CÓDIGO
    private var notificationHelper: NotificationHelper? = null

    // Esta función inicia la "escucha" en tiempo real
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
                    val list = snapshots.toObjects(Appointment::class.java)
                    // Ordenamos: las más nuevas primero
                    _appointments.value = list.sortedByDescending { it.createdAt }
                    _isLoading.value = false

                    // === DETECTOR DE CAMBIOS (NOTIFICACIONES) ===
                    for (dc in snapshots.documentChanges) {
                        val appt = dc.document.toObject(Appointment::class.java)

                        // CASO 1: SE CREÓ UNA NUEVA CITA (ADDED)
                        if (dc.type == DocumentChange.Type.ADDED) {
                            // Filtro de tiempo: Solo notificamos si se creó hace menos de 1 minuto
                            // (Para que no suenen todas las citas viejas al abrir la app)
                            val isRecent = (System.currentTimeMillis() - appt.createdAt) < 60000

                            if (isRecent) {
                                notificationHelper?.showNewBookingNotification(
                                    customerName = appt.customerName,
                                    time = "${appt.date} ${appt.time}"
                                )
                            }
                        }

                        // CASO 2: SE CANCELÓ UNA CITA (REMOVED)
                        else if (dc.type == DocumentChange.Type.REMOVED) {
                            // Al borrar no podemos ver la fecha de creación, así que asumimos que acaba de pasar
                            notificationHelper?.showCancellationNotification(
                                customerName = appt.customerName,
                                time = "${appt.date} ${appt.time}"
                            )
                        }
                    }
                }
            }
    }
}