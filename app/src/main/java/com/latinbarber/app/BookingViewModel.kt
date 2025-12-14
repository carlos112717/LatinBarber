package com.latinbarber.app

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.latinbarber.app.model.Appointment
import com.latinbarber.app.model.Barber
import com.latinbarber.app.model.Service
import com.latinbarber.app.model.ShopConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookingViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Listas de datos
    private val _barbers = MutableStateFlow<List<Barber>>(emptyList())
    val barbers = _barbers.asStateFlow()

    private val _services = MutableStateFlow<List<Service>>(emptyList())
    val services = _services.asStateFlow()

    // Horas disponibles (Ya filtradas)
    private val _availableSlots = MutableStateFlow<List<String>>(emptyList())
    val availableSlots = _availableSlots.asStateFlow()

    var selectedBarber: Barber? = null
    var selectedService: Service? = null

    private val _selectedDate = MutableStateFlow<String>("")
    val selectedDate = _selectedDate.asStateFlow()

    private val _selectedTime = MutableStateFlow<String>("")
    val selectedTime = _selectedTime.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    // Guardamos la configuración de la tienda para reusarla
    private var shopOpenTime = "09:00"
    private var shopCloseTime = "20:00"

    init {
        fetchBarbers()
        fetchServices()
        fetchShopConfig()
    }

    private fun fetchShopConfig() {
        firestore.collection("config").document("general").get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val config = doc.toObject(ShopConfig::class.java)
                    if (config != null) {
                        shopOpenTime = config.openTime
                        shopCloseTime = config.closeTime
                    }
                }
            }
    }

    // --- LÓGICA DE FILTRADO ---

    // 1. Cuando seleccionas fecha, buscamos qué horas están ocupadas
    fun onDateSelected(date: Long) {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateString = formatter.format(java.util.Date(date))
        _selectedDate.value = dateString

        // Llamamos a cargar los horarios libres
        loadAvailableSlots(dateString)
    }

    private fun loadAvailableSlots(date: String) {
        val barberName = selectedBarber?.name ?: return

        // Consultamos Firebase: "Dame todas las citas de ESTE barbero en ESTA fecha"
        firestore.collection("appointments")
            .whereEqualTo("barberName", barberName)
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { result ->
                // Creamos una lista con las horas que YA están ocupadas
                val occupiedTimes = result.documents.mapNotNull { doc ->
                    doc.getString("time")
                }

                // Generamos todas las horas y restamos las ocupadas
                generateTimeSlots(shopOpenTime, shopCloseTime, occupiedTimes)
            }
            .addOnFailureListener {
                // Si falla, mostramos todo por defecto
                generateTimeSlots(shopOpenTime, shopCloseTime, emptyList())
            }
    }

    private fun generateTimeSlots(openTime: String, closeTime: String, occupiedTimes: List<String>) {
        val slots = mutableListOf<String>()
        try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val start = Calendar.getInstance().apply { time = sdf.parse(openTime)!! }
            val end = Calendar.getInstance().apply { time = sdf.parse(closeTime)!! }

            while (start.before(end)) {
                val slotTime = sdf.format(start.time)

                // SOLO AGREGAMOS SI NO ESTÁ OCUPADA
                if (!occupiedTimes.contains(slotTime)) {
                    slots.add(slotTime)
                }

                start.add(Calendar.MINUTE, 60)
            }
            _availableSlots.value = slots
        } catch (e: Exception) {
            e.printStackTrace()
            _availableSlots.value = emptyList()
        }
    }

    // ... resto de funciones (fetchBarbers, saveBooking, onTimeSelected) igual que antes ...

    private fun fetchBarbers() {
        firestore.collection("barbers").get().addOnSuccessListener {
            _barbers.value = it.toObjects(Barber::class.java)
        }
    }

    private fun fetchServices() {
        firestore.collection("services").get().addOnSuccessListener {
            _services.value = it.toObjects(Service::class.java)
        }
    }

    fun onTimeSelected(time: String) { _selectedTime.value = time }

    fun saveBooking(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = auth.currentUser ?: return onError("No logueado")

        // DOBLE CHEQUEO DE SEGURIDAD (Por si dos personas reservan al mismo milisegundo)
        val barberName = selectedBarber?.name ?: ""
        val date = _selectedDate.value
        val time = _selectedTime.value

        firestore.collection("appointments")
            .whereEqualTo("barberName", barberName)
            .whereEqualTo("date", date)
            .whereEqualTo("time", time)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    onError("¡Lo siento! Alguien acaba de ganar esta hora. Elige otra.")
                    // Recargamos los slots para que se actualice la vista
                    loadAvailableSlots(date)
                } else {
                    // Si está libre, procedemos a guardar (tu código original de guardar)
                    performSave(user, onSuccess, onError)
                }
            }
    }

    private fun performSave(user: com.google.firebase.auth.FirebaseUser, onSuccess: () -> Unit, onError: (String) -> Unit) {
        _isSaving.value = true
        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                val realName = document.getString("name") ?: user.email ?: "Cliente"
                val realEmail = user.email ?: ""

                val newAppointment = Appointment(
                    userId = user.uid,
                    customerName = realName,
                    customerEmail = realEmail,
                    barberName = selectedBarber?.name ?: "Sin asignar",
                    serviceName = selectedService?.name ?: "Servicio",
                    price = selectedService?.price ?: 0.0,
                    date = _selectedDate.value,
                    time = _selectedTime.value,
                    status = "confirmada"
                )

                firestore.collection("appointments").add(newAppointment)
                    .addOnSuccessListener {
                        _isSaving.value = false
                        onSuccess()
                    }
                    .addOnFailureListener {
                        _isSaving.value = false
                        onError(it.message ?: "Error")
                    }
            }
    }
}