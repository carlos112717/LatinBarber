package com.latinbarber.app

import androidx.lifecycle.ViewModel
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

    init {
        fetchAllAppointments()
    }

    private fun fetchAllAppointments() {
        _isLoading.value = true

        firestore.collection("appointments")
            // .orderBy("date") // Podríamos ordenar por fecha si creamos un índice en Firebase
            .get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(Appointment::class.java)
                // Ordenamos en la app para evitar configurar índices complejos ahora mismo
                _appointments.value = list.sortedByDescending { it.createdAt } // Las más recientes primero
                _isLoading.value = false
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }
}