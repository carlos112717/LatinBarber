package com.latinbarber.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.latinbarber.app.ui.theme.BlackBackground
import com.latinbarber.app.ui.theme.DarkSurface
import com.latinbarber.app.ui.theme.GoldPrimary
import com.latinbarber.app.ui.theme.WhiteText

@Composable
fun HomeScreen(
    userName: String,
    onLogout: () -> Unit,
    onBookAppointment: () -> Unit,
    onMyAppointments: () -> Unit,
    onProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(24.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Hola, $userName", color = Color.Gray, fontSize = 14.sp)
                Text(
                    text = "Bienvenido a Barbas Cut's",
                    color = WhiteText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            // Botón Salir pequeño
            IconButton(onClick = onLogout) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir", tint = GoldPrimary)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Tarjeta Gigante: AGENDAR CITA (La más importante)
        ActionCard(
            title = "Reservar Cita",
            subtitle = "Elige tu estilo y barbero",
            icon = Icons.Default.CalendarMonth,
            onClick = onBookAppointment,
            isPrimary = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Otras opciones
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                ActionCard(
                    title = "Mis Citas",
                    subtitle = "Historial",
                    icon = Icons.Default.History,
                    onClick = onMyAppointments,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.weight(1f)) {
                ActionCard(
                    title = "Perfil",
                    subtitle = "Mis datos",
                    icon = Icons.Default.Person,
                    onClick = onProfile
                )
            }
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isPrimary: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) GoldPrimary else DarkSurface
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isPrimary) 160.dp else 140.dp) // La principal es más grande
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPrimary) BlackBackground else GoldPrimary,
                modifier = Modifier.size(40.dp)
            )
            Column {
                Text(
                    text = title,
                    color = if (isPrimary) BlackBackground else WhiteText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = subtitle,
                    color = if (isPrimary) BlackBackground.copy(alpha = 0.7f) else Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}