package com.latinbarber.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
                        AdminAppointmentCard(appointment)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAppointmentCard(appointment: Appointment) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- FILA SUPERIOR: DATOS DEL CLIENTE Y PRECIO ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically // Centrado vertical
            ) {
                // Icono de persona
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Columna central (Nombre y Correo) - Le damos peso 1f
                Column(modifier = Modifier.weight(1f)) {
                    // 1. NOMBRE DEL CLIENTE (Grande y visible)
                    Text(
                        text = appointment.customerName.ifEmpty { "Cliente" },
                        color = WhiteText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    /* 2. CORREO ELECTRÓNICO (Ahora sí mostramos el correo)
                    Text(
                        // Si el campo tiene email lo muestra, si no (citas viejas), muestra "Sin correo"
                       text = appointment.customerEmail.ifEmpty { "Sin correo" },
                        color = Color.Gray,
                        fontSize = 14.sp, // Un poquito más grande para que se lea bien
                        maxLines = 1
                    )*/
                }

                Spacer(modifier = Modifier.width(8.dp))

                // PRECIO (Siempre a la derecha)
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

            // --- FILA INFERIOR: DETALLES DE LA CITA ---

            // Servicio y Barbero
            Text(
                text = "${appointment.serviceName} con ${appointment.barberName}",
                color = WhiteText,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Fecha y Hora (Con iconos pequeños si quieres)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Puedes usar un icono de calendario aquí si quieres
                Text(
                    text = "📅 ${appointment.date}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "⏰ ${appointment.time}",
                    color = Color.Gray, // O GoldPrimary si quieres resaltarlo
                    fontSize = 14.sp
                )
            }
        }
    }
    }