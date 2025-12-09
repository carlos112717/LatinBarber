package com.latinbarber.app

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.latinbarber.app.model.ShopConfig
import com.latinbarber.app.ui.theme.BlackBackground
import com.latinbarber.app.ui.theme.DarkSurface
import com.latinbarber.app.ui.theme.GoldPrimary
import com.latinbarber.app.ui.theme.WhiteText

class AdminHoursViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    var config = mutableStateOf(ShopConfig())

    init {
        // Cargar configuración al iniciar
        firestore.collection("config").document("general").get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    doc.toObject(ShopConfig::class.java)?.let { config.value = it }
                }
            }
    }

    fun saveConfig(open: String, close: String, context: android.content.Context) {
        val newConfig = ShopConfig(openTime = open, closeTime = close)
        firestore.collection("config").document("general").set(newConfig)
            .addOnSuccessListener {
                Toast.makeText(context, "Horario Actualizado", Toast.LENGTH_SHORT).show()
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHoursScreen(
    onBack: () -> Unit,
    viewModel: AdminHoursViewModel = viewModel()
) {
    val currentConfig by viewModel.config
    val context = LocalContext.current

    // Estados locales para editar
    var openTime by remember { mutableStateOf("") }
    var closeTime by remember { mutableStateOf("") }

    // Cargar datos cuando lleguen de Firebase
    LaunchedEffect(currentConfig) {
        if (openTime.isEmpty()) openTime = currentConfig.openTime
        if (closeTime.isEmpty()) closeTime = currentConfig.closeTime
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Horarios de Atención", color = WhiteText) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Schedule, null, tint = GoldPrimary, modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Configuración General", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = openTime,
                        onValueChange = { openTime = it },
                        label = { Text("Hora Apertura (ej. 09:00)", color = Color.Gray) },
                        colors = fieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = closeTime,
                        onValueChange = { closeTime = it },
                        label = { Text("Hora Cierre (ej. 20:00)", color = Color.Gray) },
                        colors = fieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.saveConfig(openTime, closeTime, context) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Default.Save, null, tint = BlackBackground)
                Spacer(modifier = Modifier.width(8.dp))
                Text("GUARDAR HORARIOS", color = BlackBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}