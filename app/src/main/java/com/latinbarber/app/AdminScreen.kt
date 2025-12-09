package com.latinbarber.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.latinbarber.app.ui.theme.BlackBackground
import com.latinbarber.app.ui.theme.GoldPrimary
import com.latinbarber.app.ui.theme.WhiteText

@Composable
fun AdminScreen(
    onLogout: () -> Unit,
    onViewAppointments: () -> Unit,
    onManageBarbers: () -> Unit,
    onManageServices: () -> Unit,
    onManageHours: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(24.dp)
    ) {
        // Encabezado Admin
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Modo Administrador", color = GoldPrimary, fontSize = 14.sp)
                Text(
                    text = "Gestión Latin Barber",
                    color = WhiteText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onLogout) {
                Icon(Icons.Default.ExitToApp, "Salir", tint = Color.Red)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 1. VER TODAS LAS CITAS - AGENDA DE CITAS
        ActionCard(
            title = "Agenda de Citas",
            subtitle = "Ver y administrar reservas",
            icon = Icons.Default.ListAlt,
            onClick = { onViewAppointments() },
            isPrimary = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. GESTIONAR BARBEROS (¡AQUÍ ESTABA EL ERROR!)
        ActionCard(
            title = "Barberos",
            subtitle = "Agregar o eliminar personal",
            icon = Icons.Default.PersonAdd,
            // 👇 CORREGIDO: Agregamos () para que SE EJECUTE al hacer clic
            onClick = { onManageBarbers() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. GESTIONAR SERVICIOS
        ActionCard(
            title = "Servicios y Precios",
            subtitle = "Editar catálogo",
            icon = Icons.Default.ContentCut,
            onClick = {onManageServices()}
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. GESTIONAR HORARIOS
        ActionCard(
            title = "Horarios",
            subtitle = "Apertura y Cierre",
            icon = Icons.Default.Schedule, // Necesita import androidx.compose.material.icons.filled.Schedule
            onClick = { onManageHours() }
        )
    }
}