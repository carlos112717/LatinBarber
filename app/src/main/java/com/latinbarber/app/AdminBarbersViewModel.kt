package com.latinbarber.app

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.latinbarber.app.model.Barber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminBarbersViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _barbers = MutableStateFlow<List<Barber>>(emptyList())
    val barbers = _barbers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchBarbers()
    }

    // 1. LEER BARBEROS
    fun fetchBarbers() {
        _isLoading.value = true
        firestore.collection("barbers").get()
            .addOnSuccessListener { result ->
                // Mapeamos los documentos para incluir el ID (vital para poder borrarlos luego)
                val list = result.documents.mapNotNull { doc ->
                    doc.toObject(Barber::class.java)?.copy(id = doc.id)
                }
                _barbers.value = list
                _isLoading.value = false
            }
            .addOnFailureListener { _isLoading.value = false }
    }

    // 2. AGREGAR BARBERO
    fun addBarber(name: String) {
        if (name.isBlank()) return

        _isLoading.value = true
        val newBarber = Barber(name = name, rating = 5.0) // Rating inicial por defecto

        firestore.collection("barbers").add(newBarber)
            .addOnSuccessListener {
                fetchBarbers() // Recargamos la lista
            }
    }

    // 3. ELIMINAR BARBERO
    fun deleteBarber(barberId: String) {
        _isLoading.value = true
        firestore.collection("barbers").document(barberId).delete()
            .addOnSuccessListener {
                fetchBarbers() // Recargamos la lista
            }
    }
}