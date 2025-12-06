package com.latinbarber.app

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.latinbarber.app.model.Appointment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppointmentsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments = _appointments.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchMyAppointments()
    }

    private fun fetchMyAppointments() {
        val userId = auth.currentUser?.uid
        if (userId == null) return

        _isLoading.value = true

        firestore.collection("appointments")
            .whereEqualTo("userId", userId) // ¡Filtro importante!
            .get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(Appointment::class.java)
                // Ordenamos por fecha (simple) o creación
                _appointments.value = list.sortedByDescending { it.createdAt }
                _isLoading.value = false
            }
            .addOnFailureListener {
                _isLoading.value = false
                // Aquí podrías manejar errores
            }
    }
}