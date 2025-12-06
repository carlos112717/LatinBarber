package com.latinbarber.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.latinbarber.app.ui.theme.BlackBackground
import com.latinbarber.app.ui.theme.DarkSurface
import com.latinbarber.app.ui.theme.GoldPrimary
import com.latinbarber.app.ui.theme.WhiteText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBarbersScreen(
    onBack: () -> Unit,
    viewModel: AdminBarbersViewModel = viewModel()
) {
    val barbers by viewModel.barbers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Estado para el diálogo de "Agregar Barbero"
    var showAddDialog by remember { mutableStateOf(false) }
    var newBarberName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Barberos", color = WhiteText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BlackBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GoldPrimary
            ) {
                Icon(Icons.Default.Add, "Agregar", tint = BlackBackground)
            }
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
            } else if (barbers.isEmpty()) {
                Text("No hay barberos registrados", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(barbers) { barber ->
                        BarberItemCard(
                            name = barber.name,
                            onDelete = { viewModel.deleteBarber(barber.id) }
                        )
                    }
                }
            }
        }

        // DIÁLOGO PARA AGREGAR
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = DarkSurface,
                title = { Text("Nuevo Barbero", color = GoldPrimary) },
                text = {
                    OutlinedTextField(
                        value = newBarberName,
                        onValueChange = { newBarberName = it },
                        label = { Text("Nombre", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText,
                            cursorColor = GoldPrimary
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.addBarber(newBarberName)
                        newBarberName = ""
                        showAddDialog = false
                    }) { Text("Guardar", color = GoldPrimary) }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancelar", color = Color.Gray) }
                }
            )
        }
    }
}

@Composable
fun BarberItemCard(name: String, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = WhiteText)
                Spacer(modifier = Modifier.width(16.dp))
                Text(name, color = WhiteText, fontSize = 18.sp)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Eliminar", tint = Color.Red)
            }
        }
    }
}