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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    // Aquí podrías guardar si aceptó o no
                }
            )

            SideEffect {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

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
                            val userName by authViewModel.userName.collectAsState()
                            HomeScreen(
                                userName = userName,
                                onLogout = { navController.navigate("auth") },
                                onBookAppointment = { navController.navigate("booking") },
                                onMyAppointments = { navController.navigate("appointments") },
                                onProfile = { navController.navigate("profile") }
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
                                onManageBarbers = { navController.navigate("admin_barbers") },
                                onManageServices = { navController.navigate("admin_services") },
                                onManageHours = { navController.navigate("admin_hours") }
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

                        composable("admin_services") {
                            AdminServicesScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("profile") {
                            ProfileScreen(
                                onBack = { navController.popBackStack() },
                                onAccountDeleted = {
                                    // Si borra la cuenta, lo mandamos al login y limpiamos historial
                                    navController.navigate("auth") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}