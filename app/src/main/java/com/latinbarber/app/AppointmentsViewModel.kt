package com.latinbarber.app

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.latinbarber.app.model.Appointment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Locale

class AppointmentsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments = _appointments.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    // Para mensajes de error o éxito al cancelar
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
                val list = result.documents.mapNotNull { doc ->
                    doc.toObject(Appointment::class.java)?.copy(id = doc.id) // Importante copiar el ID del documento
                }
                _appointments.value = list.sortedByDescending { it.createdAt }
                _isLoading.value = false
            }
            .addOnFailureListener { _isLoading.value = false }
    }

    fun cancelAppointment(appointment: Appointment) {
        // 1. MATEMÁTICA DE FECHAS
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val appointmentDateStr = "${appointment.date} ${appointment.time}"
            val appointmentDate = sdf.parse(appointmentDateStr)

            if (appointmentDate != null) {
                val currentTime = System.currentTimeMillis()
                val appointmentTime = appointmentDate.time

                // Calculamos la diferencia en horas
                val diffInMillis = appointmentTime - currentTime
                val diffInHours = diffInMillis / (1000 * 60 * 60)

                if (diffInHours < 8) {
                    _toastMessage.value = "No puedes cancelar con menos de 8 horas de antelación."
                    return
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Si falla el cálculo, permitimos cancelar por seguridad o mostramos error
        }

        // 2. BORRAR SI CUMPLE LA REGLA
        _isLoading.value = true
        firestore.collection("appointments").document(appointment.id).delete()
            .addOnSuccessListener {
                _toastMessage.value = "Cita cancelada correctamente"
                fetchMyAppointments() // Recargar lista
            }
            .addOnFailureListener {
                _isLoading.value = false
                _toastMessage.value = "Error al cancelar"
            }
    }

    fun clearToast() { _toastMessage.value = null }

}

