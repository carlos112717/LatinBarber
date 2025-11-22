package com.latinbarber.app

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.latinbarber.app.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Por favor llena todos los campos")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                // Al loguearse, deberíamos buscar su rol, lo haremos en el siguiente paso
                _authState.value = AuthState.Success("Bienvenido de nuevo")
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error("Error: ${it.message}")
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
                    // CREAMOS EL USUARIO EN LA BASE DE DATOS
                    val newUser = User(
                        id = uid,
                        name = name,
                        email = email,
                        phone = phone,
                        role = "client" // Rol por defecto
                    )

                    firestore.collection("users").document(uid).set(newUser)
                        .addOnSuccessListener {
                            _authState.value = AuthState.Success("Cuenta creada exitosamente")
                        }
                        .addOnFailureListener { e ->
                            _authState.value = AuthState.Error("Usuario creado, pero falló al guardar datos: ${e.message}")
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