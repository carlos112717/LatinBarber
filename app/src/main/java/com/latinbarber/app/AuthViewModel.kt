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

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole = _userRole.asStateFlow()

    // 👇 NUEVO: Variable para guardar el nombre del usuario
    private val _userName = MutableStateFlow<String>("")
    val userName = _userName.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Por favor llena todos los campos")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                fetchUserProfile(it.user?.uid) // Cambiamos el nombre de la función para que tenga sentido
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error("Error: ${it.message}")
            }
    }

    // 👇 Esta función ahora busca ROL y NOMBRE
    private fun fetchUserProfile(uid: String?) {
        if (uid == null) return

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role") ?: "client"
                    val name = document.getString("name") ?: "Cliente" // Leemos el nombre

                    _userRole.value = role
                    _userName.value = name // Guardamos el nombre

                    _authState.value = AuthState.Success("Bienvenido $name")
                } else {
                    _authState.value = AuthState.Error("Usuario no encontrado")
                }
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error("Error al obtener datos")
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
                            _userRole.value = "client"
                            _userName.value = name // Guardamos el nombre al registrar
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