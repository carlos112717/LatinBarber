package com.latinbarber.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

    // Necesario para guardar archivos (Storage Access Framework)
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
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = GoldPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BlackBackground)
            )
        },
        containerColor = BlackBackground
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Selecciona el rango de fechas:", color = GoldPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            // Campos de texto simples para las fechas (Formato DD/MM/AAAA)
            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text("Fecha Inicio (dd/MM/yyyy)", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, focusedBorderColor = GoldPrimary),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text("Fecha Fin (dd/MM/yyyy)", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, focusedBorderColor = GoldPrimary),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isGenerating = true
                    generateReport(startDate, endDate) { csvContent ->
                        isGenerating = false
                        csvContentToSave = csvContent
                        // Abrir selector de dónde guardar
                        saveFileLauncher.launch("Reporte_LatinBarber.csv")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isGenerating && startDate.isNotEmpty() && endDate.isNotEmpty()
            ) {
                if (isGenerating) CircularProgressIndicator(color = BlackBackground, modifier = Modifier.size(24.dp))
                else Row {
                    Icon(Icons.Default.Download, null, tint = BlackBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DESCARGAR EXCEL (CSV)", color = BlackBackground)
                }
            }
        }
    }
}

fun generateReport(start: String, end: String, onResult: (String) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    try {
        val startDateObj = sdf.parse(start)
        val endDateObj = sdf.parse(end)

        firestore.collection("appointments").get().addOnSuccessListener { result ->
            val sb = StringBuilder()
            sb.append("Fecha,Hora,Cliente,Correo,Barbero,Servicio,Precio,Estado\n") // Encabezado CSV

            for (doc in result.documents) {
                val appt = doc.toObject(Appointment::class.java)
                if (appt != null) {
                    try {
                        val apptDate = sdf.parse(appt.date)
                        // Filtrar por rango
                        if (apptDate != null && !apptDate.before(startDateObj) && !apptDate.after(endDateObj)) {
                            sb.append("${appt.date},${appt.time},${appt.customerName},${appt.customerEmail},${appt.barberName},${appt.serviceName},${appt.price},${appt.status}\n")
                        }
                    } catch (e: Exception) {}
                }
            }
            onResult(sb.toString())
        }
    } catch (e: Exception) {
        onResult("Error en formato de fechas")
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