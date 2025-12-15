package com.latinbarber.app

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.latinbarber.app.model.Appointment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class AppointmentsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments = _appointments.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    init {
        fetchMyAppointments()
    }

    fun fetchMyAppointments() {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true

        firestore.collection("appointments")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val list = ArrayList<Appointment>()
                val batch = firestore.batch()
                var changesMade = false

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val now = System.currentTimeMillis()
                val threeDaysInMillis = TimeUnit.DAYS.toMillis(3)

                for (doc in result.documents) {
                    val appt = doc.toObject(Appointment::class.java)?.copy(id = doc.id)

                    if (appt != null) {
                        // BORRADO AUTOMÁTICO (Más de 3 días)
                        var shouldDelete = false
                        try {
                            val date = sdf.parse(appt.date)
                            if (date != null) {
                                if ((date.time + threeDaysInMillis) < now) {
                                    shouldDelete = true
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        if (shouldDelete) {
                            val docRef = firestore.collection("appointments").document(appt.id)
                            batch.delete(docRef)
                            changesMade = true
                        } else {
                            list.add(appt)
                        }
                    }
                }

                if (changesMade) {
                    batch.commit()
                }

                _appointments.value = list.sortedByDescending { it.createdAt }
                _isLoading.value = false
            }
            .addOnFailureListener { _isLoading.value = false }
    }

    fun cancelAppointment(appointment: Appointment) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val appointmentDateStr = "${appointment.date} ${appointment.time}"

        try {
            val appointmentDate = sdf.parse(appointmentDateStr)

            if (appointmentDate != null) {
                val currentTime = System.currentTimeMillis()
                val appointmentTime = appointmentDate.time
                val diffInMillis = appointmentTime - currentTime
                val diffInHours = diffInMillis / (1000 * 60 * 60)

                // REGLA DE ORO: Si faltan menos de 8 horas, BLOQUEAR.
                if (diffInHours < 8) {
                    _toastMessage.value = "⚠️ No puedes cancelar con menos de 8 horas de antelación."
                    return // <--- AQUÍ SE DETIENE TODO. NO BORRA.
                } else {
                    // Si pasó la prueba, AHORA SÍ borramos
                    performDelete(appointment.id)
                }
            } else {
                _toastMessage.value = "Error al verificar la fecha."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _toastMessage.value = "Error de formato de fecha."
        }
    }

    private fun performDelete(documentId: String) {
        _isLoading.value = true
        firestore.collection("appointments").document(documentId).delete()
            .addOnSuccessListener {
                _toastMessage.value = "Cita cancelada correctamente"
                fetchMyAppointments()
            }
            .addOnFailureListener {
                _isLoading.value = false
                _toastMessage.value = "Error al cancelar"
            }
    }

    fun clearToast() { _toastMessage.value = null }
}