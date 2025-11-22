package com.latinbarber.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.latinbarber.app.ui.theme.LatinBarberTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContent es el contenedor principal para dibujar en Compose
        setContent {
            // Aplicamos el tema de la app
            LatinBarberTheme {
                // Surface es como el "lienzo" o fondo base.
                // Le decimos que ocupe todo el tamaño disponible (fillMaxSize)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ¡AQUÍ ADENTRO es el lugar correcto para llamar a tu pantalla!
                    // Si esto estuviera fuera de 'Surface' o 'setContent', daría el error 3.
                    AuthScreen()
                }
            }
        }
    }
}