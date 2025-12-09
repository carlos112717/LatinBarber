package com.latinbarber.app

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.latinbarber.app.model.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminServicesViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _services = MutableStateFlow<List<Service>>(emptyList())
    val services = _services.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchServices()
    }

    // 1. LEER SERVICIOS
    fun fetchServices() {
        _isLoading.value = true
        firestore.collection("services").get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { doc ->
                    doc.toObject(Service::class.java)?.copy(id = doc.id)
                }
                _services.value = list
                _isLoading.value = false
            }
            .addOnFailureListener { _isLoading.value = false }
    }

    // 2. AGREGAR SERVICIO
    fun addService(name: String, price: Double, duration: Int) {
        if (name.isBlank()) return

        _isLoading.value = true
        val newService = Service(
            name = name,
            price = price,
            durationMinutes = duration
        )

        firestore.collection("services").add(newService)
            .addOnSuccessListener {
                fetchServices() // Recargar lista
            }
    }

    // 3. ELIMINAR SERVICIO
    fun deleteService(serviceId: String) {
        _isLoading.value = true
        firestore.collection("services").document(serviceId).delete()
            .addOnSuccessListener {
                fetchServices()
            }
    }
}