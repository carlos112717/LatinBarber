package com.latinbarber.app

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage = _statusMessage.asStateFlow()

    // Datos actuales del usuario
    var currentName = MutableStateFlow("")
    var currentPhone = MutableStateFlow("")
    var currentEmail = MutableStateFlow("")

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                currentName.value = doc.getString("name") ?: ""
                currentPhone.value = doc.getString("phone") ?: ""
                currentEmail.value = doc.getString("email") ?: ""
            }
    }

    fun updateProfile(newName: String, newPhone: String) {
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true

        val updates = mapOf(
            "name" to newName,
            "phone" to newPhone
        )

        firestore.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                _isLoading.value = false
                _statusMessage.value = "Datos actualizados correctamente"
            }
            .addOnFailureListener {
                _isLoading.value = false
                _statusMessage.value = "Error al actualizar"
            }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true

        // 1. Borrar de base de datos
        firestore.collection("users").document(uid).delete()
            .addOnSuccessListener {
                // 2. Borrar autenticación (El usuario real)
                auth.currentUser?.delete()
                    ?.addOnSuccessListener {
                        _isLoading.value = false
                        onSuccess()
                    }
                    ?.addOnFailureListener {
                        _isLoading.value = false
                        _statusMessage.value = "Error de seguridad. Cierra sesión y vuelve a entrar para borrar."
                    }
            }
            .addOnFailureListener {
                _isLoading.value = false
                _statusMessage.value = "Error al borrar datos"
            }
    }

    fun clearMessage() { _statusMessage.value = null }
}