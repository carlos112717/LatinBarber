package com.latinbarber.app

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.latinbarber.app.model.Appointment
import com.latinbarber.app.ui.theme.*
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(onBack: () -> Unit) {
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val context = LocalContext.current
    var csvContentToSave by remember { mutableStateOf("") }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { saveCsvToUri(context, it, csvContentToSave) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generar Reportes", color = WhiteText) },
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
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Selecciona el rango de fechas:", color = GoldPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            // FECHA INICIO (Colores integrados aquí mismo para evitar errores)
            OutlinedTextField(
                value = startDate,
                onValueChange = { },
                label = { Text("Fecha Inicio", color = Color.Gray) },
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.DateRange, null, tint = GoldPrimary, modifier = Modifier.clickable { showStartPicker = true })
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText,
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth().clickable { showStartPicker = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // FECHA FIN
            OutlinedTextField(
                value = endDate,
                onValueChange = { },
                label = { Text("Fecha Fin", color = Color.Gray) },
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.DateRange, null, tint = GoldPrimary, modifier = Modifier.clickable { showEndPicker = true })
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText,
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth().clickable { showEndPicker = true }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isGenerating = true
                    generateReport(startDate, endDate) { csvContent ->
                        isGenerating = false
                        if (csvContent.isEmpty()) {
                            Toast.makeText(context, "No se encontraron datos en este rango", Toast.LENGTH_SHORT).show()
                        } else {
                            csvContentToSave = csvContent
                            saveFileLauncher.launch("Reporte_LatinBarber.csv")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isGenerating && startDate.isNotEmpty() && endDate.isNotEmpty()
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = BlackBackground, modifier = Modifier.size(24.dp))
                } else {
                    Row {
                        Icon(Icons.Default.Download, null, tint = BlackBackground)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DESCARGAR EXCEL (CSV)", color = BlackBackground)
                    }
                }
            }
        }

        // DIALOGOS DE CALENDARIO (Colores integrados aquí mismo)
        if (showStartPicker) {
            DatePickerDialog(
                onDismissRequest = { showStartPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            startDate = convertMillisToDate(it)
                        }
                        showStartPicker = false
                    }) { Text("OK", color = GoldPrimary) }
                },
                colors = DatePickerDefaults.colors(containerColor = DarkSurface)
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = DarkSurface,
                        titleContentColor = GoldPrimary,
                        headlineContentColor = WhiteText,
                        weekdayContentColor = WhiteText,
                        dayContentColor = WhiteText,
                        selectedDayContainerColor = GoldPrimary,
                        selectedDayContentColor = BlackBackground,
                        todayContentColor = GoldPrimary,
                        todayDateBorderColor = GoldPrimary,
                        yearContentColor = WhiteText,
                        currentYearContentColor = GoldPrimary,
                        selectedYearContainerColor = GoldPrimary,
                        selectedYearContentColor = BlackBackground
                    )
                )
            }
        }

        if (showEndPicker) {
            DatePickerDialog(
                onDismissRequest = { showEndPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            endDate = convertMillisToDate(it)
                        }
                        showEndPicker = false
                    }) { Text("OK", color = GoldPrimary) }
                },
                colors = DatePickerDefaults.colors(containerColor = DarkSurface)
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = DarkSurface,
                        titleContentColor = GoldPrimary,
                        headlineContentColor = WhiteText,
                        weekdayContentColor = WhiteText,
                        dayContentColor = WhiteText,
                        selectedDayContainerColor = GoldPrimary,
                        selectedDayContentColor = BlackBackground,
                        todayContentColor = GoldPrimary,
                        todayDateBorderColor = GoldPrimary,
                        yearContentColor = WhiteText,
                        currentYearContentColor = GoldPrimary,
                        selectedYearContainerColor = GoldPrimary,
                        selectedYearContentColor = BlackBackground
                    )
                )
            }
        }
    }
}

// === FUNCIONES AUXILIARES DE LÓGICA (Ya no hay funciones de color) ===

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

fun generateReport(start: String, end: String, onResult: (String) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    try {
        val startDateObj = sdf.parse(start)
        val endDateObj = sdf.parse(end)

        firestore.collection("appointments").get().addOnSuccessListener { result ->
            val sb = StringBuilder()
            sb.append("\uFEFF") // BOM para Excel
            sb.append("Fecha,Hora,Cliente,Correo,Barbero,Servicio,Precio,Estado\n")

            var count = 0
            for (doc in result.documents) {
                val appt = doc.toObject(Appointment::class.java)
                if (appt != null) {
                    try {
                        val apptDate = sdf.parse(appt.date)
                        if (apptDate != null &&
                            (apptDate == startDateObj || apptDate.after(startDateObj)) &&
                            (apptDate == endDateObj || apptDate.before(endDateObj))) {

                            sb.append("${appt.date},${appt.time},${appt.customerName},${appt.customerEmail},${appt.barberName},${appt.serviceName},${appt.price},${appt.status}\n")
                            count++
                        }
                    } catch (e: Exception) {}
                }
            }
            if (count > 0) onResult(sb.toString()) else onResult("")
        }
    } catch (e: Exception) {
        onResult("")
    }
}

fun saveCsvToUri(context: Context, uri: Uri, content: String) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use { it.write(content) }
        }
        Toast.makeText(context, "Reporte guardado exitosamente", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
    }
}