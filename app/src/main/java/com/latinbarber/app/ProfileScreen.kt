package com.latinbarber.app

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.latinbarber.app.ui.theme.BlackBackground
import com.latinbarber.app.ui.theme.DarkSurface
import com.latinbarber.app.ui.theme.ErrorRed
import com.latinbarber.app.ui.theme.GoldPrimary
import com.latinbarber.app.ui.theme.WhiteText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val name by viewModel.currentName.collectAsState()
    val phone by viewModel.currentPhone.collectAsState()
    val email by viewModel.currentEmail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.statusMessage.collectAsState()

    val context = LocalContext.current

    // Variables locales para edición
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }

    // Cargar datos en los campos cuando lleguen de Firebase
    LaunchedEffect(name, phone) {
        if (editName.isEmpty()) editName = name
        if (editPhone.isEmpty()) editPhone = phone
    }

    // Mostrar mensajes Toast
    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    // Dialogo de confirmación para borrar
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", color = WhiteText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BlackBackground)
            )
        },
        containerColor = BlackBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // FOTO DE PERFIL (Placeholder)
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(DarkSurface)
                        .border(2.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = Color.Gray,
                        modifier = Modifier.size(60.dp)
                    )
                }
                // Botón pequeño de cámara
                IconButton(
                    onClick = { Toast.makeText(context, "Próximamente: Subir Foto", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(GoldPrimary, CircleShape)
                ) {
                    Icon(Icons.Default.CameraAlt, null, tint = BlackBackground)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(email, color = Color.Gray, fontSize = 14.sp) // El correo no se edita por seguridad

            Spacer(modifier = Modifier.height(32.dp))

            // FORMULARIO
            OutlinedTextField(
                value = editName,
                onValueChange = { editName = it },
                label = { Text("Nombre Completo", color = Color.Gray) },
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = editPhone,
                onValueChange = { editPhone = it },
                label = { Text("Celular", color = Color.Gray) },
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // BOTÓN GUARDAR
            Button(
                onClick = { viewModel.updateProfile(editName, editPhone) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = BlackBackground, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Save, null, tint = BlackBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ACTUALIZAR DATOS", color = BlackBackground, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // BOTÓN ELIMINAR CUENTA (Zona de peligro)
            TextButton(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
            ) {
                Icon(Icons.Default.DeleteForever, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Eliminar mi cuenta definitivamente")
            }
        }

        // ALERTA DE BORRADO
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor = DarkSurface,
                title = { Text("¿Estás seguro?", color = ErrorRed, fontWeight = FontWeight.Bold) },
                text = { Text("Esta acción no se puede deshacer. Se borrarán tus datos y reservas.", color = WhiteText) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteAccount(onSuccess = onAccountDeleted)
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) { Text("Sí, eliminar", color = WhiteText) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar", color = Color.Gray) }
                }
            )
        }
    }
}