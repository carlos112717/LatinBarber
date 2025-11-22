package com.latinbarber.app

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.latinbarber.app.ui.theme.BlackBackground
import com.latinbarber.app.ui.theme.DarkSurface
import com.latinbarber.app.ui.theme.GoldPrimary
import com.latinbarber.app.ui.theme.WhiteText

@Composable
fun AuthScreen(viewModel: AuthViewModel = viewModel()) {
    // Variables para guardar lo que escribe el usuario
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) } // ¿Está en Login o Registro?

    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    // Reacción a los cambios de estado (Éxito o Error)
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Error -> Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            is AuthState.Success -> Toast.makeText(context, "¡Bienvenido a Latin Barber!", Toast.LENGTH_SHORT).show()
            else -> {}
        }
    }

    // DISEÑO VISUAL
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground) // Fondo Negro
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo (Texto por ahora)
        Text(
            text = "LATIN BARBER",
            color = WhiteText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Campo Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = WhiteText,
                unfocusedTextColor = WhiteText,
                cursorColor = GoldPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña", color = Color.Gray) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = WhiteText,
                unfocusedTextColor = WhiteText,
                cursorColor = GoldPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón Principal (Dorado)
        Button(
            onClick = {
                if (isLoginMode) {
                    viewModel.login(email, password)
                } else {
                    viewModel.register(email, password)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = if (isLoginMode) "INICIAR SESIÓN" else "REGISTRARSE",
                color = BlackBackground, // Texto negro sobre botón dorado
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para cambiar entre Login y Registro
        TextButton(onClick = { isLoginMode = !isLoginMode }) {
            Text(
                text = if (isLoginMode) "¿No tienes cuenta? Regístrate aquí" else "¿Ya tienes cuenta? Inicia sesión",
                color = GoldPrimary
            )
        }
    }
}