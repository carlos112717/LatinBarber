package com.latinbarber.app

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.latinbarber.app.model.Appointment
import com.latinbarber.app.model.Barber
import com.latinbarber.app.model.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BookingViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Listas de datos
    private val _barbers = MutableStateFlow<List<Barber>>(emptyList())
    val barbers = _barbers.asStateFlow()

    private val _services = MutableStateFlow<List<Service>>(emptyList())
    val services = _services.asStateFlow()

    // Estado de la selección
    var selectedBarber: Barber? = null
    var selectedService: Service? = null

    private val _selectedDate = MutableStateFlow<String>("")
    val selectedDate = _selectedDate.asStateFlow()

    private val _selectedTime = MutableStateFlow<String>("")
    val selectedTime = _selectedTime.asStateFlow()

    // Estado de carga
    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    init {
        fetchBarbers()
        fetchServices()
    }

    private fun fetchBarbers() {
        firestore.collection("barbers").get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(Barber::class.java)
                _barbers.value = list
            }
    }

    private fun fetchServices() {
        firestore.collection("services").get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(Service::class.java)
                _services.value = list
            }
    }

    fun onDateSelected(date: Long) {
        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        _selectedDate.value = formatter.format(java.util.Date(date))
    }

    fun onTimeSelected(time: String) {
        _selectedTime.value = time
    }

    // --- FUNCIÓN CORREGIDA ---
    fun saveBooking(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = auth.currentUser

        // 1. Verificamos que el usuario exista antes de hacer nada
        if (user == null) {
            onError("No estás logueado")
            return
        }

        _isSaving.value = true

        // 2. Buscamos el nombre verdadero en la base de datos 'users'
        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                // Aquí adentro 'document' SÍ existe
                val realName = document.getString("name") ?: user.email ?: "Cliente"
                val realEmail = user.email ?: ""

                // 3. Creamos el objeto Cita
                val newAppointment = Appointment(
                    userId = user.uid,
                    customerName = realName,
                    customerEmail = realEmail, // Guardamos el correo
                    barberName = selectedBarber?.name ?: "Sin asignar",
                    serviceName = selectedService?.name ?: "Servicio",
                    price = selectedService?.price ?: 0.0,
                    date = _selectedDate.value,
                    time = _selectedTime.value,
                    status = "confirmada"
                )

                // 4. Guardamos la cita en 'appointments'
                firestore.collection("appointments")
                    .add(newAppointment)
                    .addOnSuccessListener {
                        _isSaving.value = false
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        _isSaving.value = false
                        onError(e.message ?: "Error al guardar")
                    }
            }
            .addOnFailureListener { e ->
                _isSaving.value = false
                onError("Error al obtener datos del usuario: ${e.message}")
            }
    }
}