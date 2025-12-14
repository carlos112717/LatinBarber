package com.latinbarber.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.latinbarber.app.ui.theme.BlackBackground
import com.latinbarber.app.ui.theme.DarkSurface
import com.latinbarber.app.ui.theme.GoldPrimary
import com.latinbarber.app.ui.theme.WhiteText

@Composable
fun AdminScreen(
    onLogout: () -> Unit,
    onViewAppointments: () -> Unit,
    onManageBarbers: () -> Unit,
    onManageServices: () -> Unit,
    onManageHours: () -> Unit,
    onViewReports: () -> Unit, // <--- NUEVO: Ir a Reportes
    viewModel: AdminViewModel = viewModel() // <--- NUEVO: Cerebro Global
) {
    val context = LocalContext.current

    // INICIAR EL CEREBRO (Escucha notificaciones y borra viejas)
    LaunchedEffect(Unit) {
        viewModel.initAdmin(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // --- ENCABEZADO ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Modo Administrador", color = GoldPrimary, fontSize = 14.sp)
                Text(text = "Gestión Latin Barber", color = WhiteText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onLogout) {
                Icon(Icons.Default.ExitToApp, "Salir", tint = Color.Red)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- BOTONES ---
        AdminActionCard("Agenda de Citas", "Ver y administrar reservas", Icons.Default.ListAlt, onViewAppointments, true)
        Spacer(modifier = Modifier.height(16.dp))

        AdminActionCard("Barberos", "Gestionar personal", Icons.Default.PersonAdd, onManageBarbers)
        Spacer(modifier = Modifier.height(16.dp))

        AdminActionCard("Servicios y Precios", "Editar catálogo", Icons.Default.ContentCut, onManageServices)
        Spacer(modifier = Modifier.height(16.dp))

        AdminActionCard("Horarios", "Apertura y Cierre", Icons.Default.Schedule, onManageHours)
        Spacer(modifier = Modifier.height(16.dp))

        // NUEVO BOTÓN DE REPORTES
        AdminActionCard("Reportes y Excel", "Descargar historial", Icons.Default.Assessment, onViewReports)
    }
}

@Composable
fun AdminActionCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit, isPrimary: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isPrimary) GoldPrimary else DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(100.dp).clickable { onClick() }
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = if (isPrimary) BlackBackground else GoldPrimary, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, color = if (isPrimary) BlackBackground else WhiteText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = subtitle, color = if (isPrimary) BlackBackground.copy(alpha = 0.7f) else Color.Gray, fontSize = 14.sp)
            }
        }
    }
}