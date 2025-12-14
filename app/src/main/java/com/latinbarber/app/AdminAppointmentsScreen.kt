package com.latinbarber.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.latinbarber.app.model.Appointment
import com.latinbarber.app.ui.theme.BlackBackground
import com.latinbarber.app.ui.theme.DarkSurface
import com.latinbarber.app.ui.theme.GoldPrimary
import com.latinbarber.app.ui.theme.WhiteText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppointmentsScreen(
    onBack: () -> Unit,
    viewModel: AdminAppointmentsViewModel = viewModel()
) {
    val appointments by viewModel.appointments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    // 👇 INICIAMOS LA ESCUCHA EN TIEMPO REAL AL ABRIR LA PANTALLA
    LaunchedEffect(Unit) {
        viewModel.startListening(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agenda General", color = WhiteText) },
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
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = GoldPrimary, modifier = Modifier.align(Alignment.Center))
            } else if (appointments.isEmpty()) {
                Text(
                    "No hay citas registradas aún",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(appointments) { appointment ->
                        AdminAppointmentCard(
                            appointment = appointment,
                            // 👇 ACCIÓN DE BORRAR (Directo a Firebase para máxima potencia)
                            onDelete = {
                                FirebaseFirestore.getInstance()
                                    .collection("appointments")
                                    .document(appointment.id)
                                    .delete()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAppointmentCard(appointment: Appointment, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- FILA SUPERIOR: DATOS DEL CLIENTE Y PRECIO ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono de persona
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Columna central (Nombre y Correo)
                Column(modifier = Modifier.weight(1f)) {
                    // 1. NOMBRE DEL CLIENTE
                    Text(
                        text = if (appointment.customerName.isNotEmpty()) appointment.customerName else "Cliente",
                        color = WhiteText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    // 2. CORREO ELECTRÓNICO
                    Text(
                        text = if (appointment.customerEmail.isNotEmpty()) appointment.customerEmail else "Sin correo",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // PRECIO
                Text(
                    text = "${appointment.price} €",
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // --- FILA MEDIO: DETALLES ---
            Text(
                text = "${appointment.serviceName} con ${appointment.barberName}",
                color = WhiteText,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Fecha y Hora
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📅 ${appointment.date}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "⏰ ${appointment.time}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTÓN ELIMINAR (SOLO ADMIN) ---
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.9f)),
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Delete, null, tint = WhiteText, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("ELIMINAR CITA", color = WhiteText, fontWeight = FontWeight.Bold)
            }
        }
    }
}