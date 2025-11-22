package com.latinbarber.app

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Estado para saber si estamos cargando, si hubo error, o si entramos
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    fun login(email: String, pass: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                _authState.value = AuthState.Success
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Error desconocido")
            }
    }

    fun register(email: String, pass: String) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                // Aquí luego guardaremos datos extra del usuario en base de datos
                _authState.value = AuthState.Success
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Error al registrar")
            }
    }
}

// Estados posibles de la autenticación
sealed class AuthState {
    object Idle : AuthState() // Esperando
    object Loading : AuthState() // Cargando
    object Success : AuthState() // Éxito
    data class Error(val message: String) : AuthState()
}