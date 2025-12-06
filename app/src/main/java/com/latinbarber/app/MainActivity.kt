package com.latinbarber.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.latinbarber.app.ui.theme.LatinBarberTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LatinBarberTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Sistema de Navegación
                    val navController = rememberNavController()
                    // Compartimos el ViewModel para saber si el usuario está logueado
                    val authViewModel: AuthViewModel = viewModel()
                    val authState by authViewModel.authState.collectAsState()
                    val userRole by authViewModel.userRole.collectAsState() // <--- OJO AQUÍ

                    // Observamos el estado: Si el login es exitoso, navegamos a Home
                    LaunchedEffect(authState, userRole) {
                        if (authState is AuthState.Success) {
                            // DECISIÓN DE RUTA BASADA EN EL ROL
                            if (userRole == "admin") {
                                navController.navigate("admin_home") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            } else {
                                navController.navigate("home") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                        }
                    }

                    NavHost(navController = navController, startDestination = "auth") {
                        composable("auth") { AuthScreen(viewModel = authViewModel) }

                        composable("home") {
                            HomeScreen(
                                onLogout = { navController.navigate("auth") },
                                onBookAppointment = { navController.navigate("booking") },
                                onMyAppointments = { navController.navigate("appointments") } // <--- CONEXIÓN
                            )
                        }

                        // 👇 AGREGAMOS LA NUEVA PANTALLA
                        composable("booking") {
                            BookingScreen(
                                onBack = { navController.popBackStack() } // Para volver al Home
                            )
                        }

                        composable("appointments") {
                            AppointmentsScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("admin_home") {
                            AdminScreen(
                                onLogout = { navController.navigate("auth") },
                                onViewAppointments = { navController.navigate("admin_appointments") },
                                onManageBarbers = { navController.navigate("admin_barbers") } // <--- ¡LISTO!
                            )
                        }

                        composable("admin_appointments") {
                            AdminAppointmentsScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("admin_barbers") {
                            AdminBarbersScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}