package com.latinbarber.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.latinbarber.app.ui.theme.LatinBarberTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Mantenemos el Theme para que la app funcione bien
            LatinBarberTheme {
                // Surface es un contenedor básico. Le decimos que ocupe todo el espacio.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ¡AQUÍ LLAMAMOS A NUESTRA PANTALLA!
                    AuthScreen()
                }
            }
        }
    }
}
