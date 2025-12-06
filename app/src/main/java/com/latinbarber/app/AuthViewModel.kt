package com.latinbarber.app

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.latinbarber.app.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Estado de autenticación
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    // Estado del Rol (Para saber a dónde navegar)
    private val _userRole = MutableStateFlow<String?>(null)
    val userRole = _userRole.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Por favor llena todos los campos")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                // Al loguearse, buscamos el ROL inmediatamente
                fetchUserRole(it.user?.uid)
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error("Error: ${it.message}")
            }
    }

    private fun fetchUserRole(uid: String?) {
        if (uid == null) return

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role") ?: "client"
                    _userRole.value = role // Guardamos el rol en el estado
                    _authState.value = AuthState.Success("Bienvenido") // ¡Ahora sí damos el éxito!
                } else {
                    _authState.value = AuthState.Error("Usuario no encontrado en base de datos")
                }
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error("Error al obtener datos de usuario")
            }
    }

    fun register(email: String, pass: String, name: String, phone: String) {
        if (email.isBlank() || pass.isBlank() || name.isBlank() || phone.isBlank()) {
            _authState.value = AuthState.Error("Todos los datos son obligatorios")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    val newUser = User(
                        id = uid,
                        name = name,
                        email = email,
                        phone = phone,
                        role = "client"
                    )
                    firestore.collection("users").document(uid).set(newUser)
                        .addOnSuccessListener {
                            _userRole.value = "client" // Al registrarse siempre es cliente
                            _authState.value = AuthState.Success("Cuenta creada exitosamente")
                        }
                        .addOnFailureListener { e ->
                            _authState.value = AuthState.Error("Fallo al guardar datos: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Error al registrar")
            }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}