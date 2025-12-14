package com.latinbarber.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookingScreen(
    onBack: () -> Unit,
    viewModel: BookingViewModel = viewModel()
) {
    val barbers by viewModel.barbers.collectAsState()
    val services by viewModel.services.collectAsState()
    val date by viewModel.selectedDate.collectAsState()
    val time by viewModel.selectedTime.collectAsState()
    val availableSlots by viewModel.availableSlots.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current // <--- OBTENER CONTEXTO
    val notificationHelper = remember { NotificationHelper(context) } // <--- INICIALIZAR HELPER

    // Variables simples para la UI
    var step by remember { mutableStateOf(1) } // 1: Barbero, 2: Servicio, 3: Fecha

    // Estado para el calendario
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Cita", color = WhiteText) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (step > 1) step-- else onBack()
                    }) {
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
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Barra de progreso simple (Texto)
            val titleText = when (step) {
                1 -> "Paso 1: Elige tu Barbero"
                2 -> "Paso 2: Elige el Servicio"
                else -> "Paso 3: Fecha y Hora"
            }

            Text(
                text = titleText,
                color = GoldPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // === CONTENIDO SEGÚN EL PASO ===
            if (step == 1) {
                // --- PASO 1: LISTA DE BARBEROS ---
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(barbers) { barber ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectedBarber = barber
                                    step = 2 // Avanzar al siguiente paso
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(Color.Gray, RoundedCornerShape(50))
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(barber.name, color = WhiteText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text("⭐ ${barber.rating}", color = GoldPrimary)
                                }
                            }
                        }
                    }
                }
            } else if (step == 2) {
                // --- PASO 2: LISTA DE SERVICIOS ---
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(services) { service ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectedService = service
                                    step = 3 // Avanzar al paso de fecha
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = service.name,
                                        color = WhiteText,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${service.durationMinutes} min",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "${service.price} €",
                                    color = GoldPrimary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            } else if (step == 3) {
                // --- PASO 3: FECHA Y HORA ---

                // 1. SELECCIÓN DE FECHA
                Text("Fecha de la Cita:", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { showDatePicker = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, null, tint = GoldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (date.isEmpty()) "Seleccionar Fecha" else date,
                        color = WhiteText,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. SELECCIÓN DE HORA (Solo si ya hay fecha)
                if (date.isNotEmpty()) {
                    Text("Horarios Disponibles:", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableSlots.forEach { slot ->
                            val isSelected = (time == slot)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onTimeSelected(slot) },
                                label = { Text(slot, color = if(isSelected) BlackBackground else WhiteText) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    containerColor = DarkSurface
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = GoldPrimary
                                )
                            )
                        }
                    }
                }

                // 3. BOTÓN CONFIRMAR (Al final)
                if (date.isNotEmpty() && time.isNotEmpty()) {
                    Spacer(modifier = Modifier.weight(1f))

                    // Obtenemos el estado de carga
                    val isSaving by viewModel.isSaving.collectAsState()

                    Button(
                        onClick = {
                            // LLAMAMOS A LA FUNCIÓN DE GUARDAR
                            viewModel.saveBooking(
                                onSuccess = {
                                    // 1. Lanzar notificación inmediata
                                    notificationHelper.showConfirmationNotification(
                                        barberName = viewModel.selectedBarber?.name ?: "Tu Barbero",
                                        time = time
                                    )

                                    // 2. Programar la alarma (Recordatorio)
                                    notificationHelper.scheduleReminder(
                                        appointmentId = (0..10000).random(), // ID único para la alarma
                                        dateString = date, // Por ahora usaremos el tiempo relativo
                                        timeString = time
                                    )

                                    // 3. Avanzar a la pantalla de éxito
                                    step = 4
                                },
                                onError = { /* Manejo de error opcional */ }
                            )
                        },

                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !isSaving // Deshabilitar si está guardando
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = BlackBackground, modifier = Modifier.size(24.dp))
                        } else {
                            Text("CONFIRMAR RESERVA", color = BlackBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // ... cierre del paso 3 ...
            } else if (step == 4) {
                // === PASO 4: ÉXITO ===
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icono gigante de Check
                    Icon(
                        imageVector = Icons.Default.CheckCircle, // Necesita import
                        contentDescription = "Éxito",
                        tint = GoldPrimary,
                        modifier = Modifier.size(100.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "¡Reserva Exitosa!",
                        color = WhiteText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Te esperamos en Latin Barber",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = onBack, // Volver al Home
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("VOLVER AL INICIO", color = BlackBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
            }
        }

        // DIÁLOGO DEL CALENDARIO (Fuera de la columna para que se superponga bien)
        // DIÁLOGO DEL CALENDARIO (Ventana emergente)
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                // 👇 1. CAMBIO IMPORTANTE: Color de fondo de la ventana
                colors = DatePickerDefaults.colors(
                    containerColor = DarkSurface
                ),
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.onDateSelected(millis)
                        }
                        showDatePicker = false
                    }) { Text("OK", color = GoldPrimary, fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = GoldPrimary) }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    // 👇 2. CAMBIO IMPORTANTE: Colores internos del calendario
                    colors = DatePickerDefaults.colors(
                        containerColor = DarkSurface,

                        // Encabezados
                        titleContentColor = GoldPrimary,    // "Seleccione fecha"
                        headlineContentColor = WhiteText,   // La fecha escrita grande

                        // Días de la semana (Lu, Ma, Mi...)
                        weekdayContentColor = WhiteText,

                        // Días del mes (Números)
                        dayContentColor = WhiteText,
                        disabledDayContentColor = Color.Gray,

                        // Día Seleccionado (Círculo Dorado)
                        selectedDayContainerColor = GoldPrimary,
                        selectedDayContentColor = BlackBackground,

                        // Día de "Hoy" (Borde Dorado)
                        todayContentColor = GoldPrimary,
                        todayDateBorderColor = GoldPrimary,

                        // Flechas de navegación (< >)
                        navigationContentColor = GoldPrimary,

                        // Menú de Años
                        yearContentColor = WhiteText,
                        currentYearContentColor = GoldPrimary,
                        selectedYearContainerColor = GoldPrimary,
                        selectedYearContentColor = BlackBackground
                    )
                )
            }
        }
    }