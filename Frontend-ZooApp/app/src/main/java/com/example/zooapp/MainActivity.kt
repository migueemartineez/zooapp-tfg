package com.example.zooapp

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.zooapp.ui.theme.ZooAppTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.zooapp.model.Animal
import com.example.zooapp.network.RetrofitClient
import com.example.zooapp.screens.PantallaAnimales
import com.example.zooapp.service.BeaconService
import com.example.zooapp.screens.PantallaDetalleAnimal
import com.example.zooapp.screens.PantallaLogin
import com.example.zooapp.screens.PantallaLogros
import com.example.zooapp.screens.PantallaPerfil
import com.example.zooapp.screens.PantallaRegistro
import com.example.zooapp.screens.PantallaRutas
import com.example.zooapp.screens.PantallaMapa

// Mapeo zona → zonaId del backend
val zonaIds = mapOf(
    "Isla de Madagascar" to "6a00b55716e824e0e606c8f5",
    "África Ecuatorial" to "6a00b68916e824e0e606c8f7",
    "Sudeste Asiático" to "6a00b7c516e824e0e606c8f9",
    "Indo Pacífico" to "6a00b88a16e824e0e606c8fb",
    "Centro y Sudamérica" to "6a00b8d716e824e0e606c8fd"
)

class MainActivity : ComponentActivity() {

    private lateinit var beaconService: BeaconService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                ), 1234
            )
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 1234
            )
        }

        // Despertar el servidor al arrancar
        RetrofitClient.instance.obtenerAnimales()
            .enqueue(object : Callback<List<Animal>> {
                override fun onResponse(call: Call<List<Animal>>, response: Response<List<Animal>>) {}
                override fun onFailure(call: Call<List<Animal>>, t: Throwable) {}
            })

        setContent {
            ZooAppTheme {
                var zonaActual by remember { mutableStateOf("") }
                val navController = rememberNavController()
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

                val pantallasConBarra = listOf("mapa", "animales", "rutas", "logros", "perfil")

                Scaffold(
                    bottomBar = {
                        if (currentRoute in pantallasConBarra) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentRoute == "mapa",
                                    onClick = { navController.navigate("mapa") },
                                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Mapa") },
                                    label = { Text("Mapa") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "animales",
                                    onClick = { navController.navigate("animales") },
                                    icon = { Icon(Icons.Default.Star, contentDescription = "Animales") },
                                    label = { Text("Animales") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "rutas",
                                    onClick = { navController.navigate("rutas") },
                                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Rutas") },
                                    label = { Text("Rutas") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "logros",
                                    onClick = { navController.navigate("logros") },
                                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Logros") },
                                    label = { Text("Logros") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "perfil",
                                    onClick = { navController.navigate("perfil") },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                                    label = { Text("Perfil") }
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable("login") {
                            PantallaLogin(
                                onLoginExitoso = { user ->
                                    SesionUsuario.usuario = user
                                    // Cargar todos los animales al hacer login
                                    RetrofitClient.instance.obtenerAnimales()
                                        .enqueue(object : Callback<List<Animal>> {
                                            override fun onResponse(call: Call<List<Animal>>, response: Response<List<Animal>>) {
                                                SesionUsuario.todosLosAnimales = response.body()
                                            }
                                            override fun onFailure(call: Call<List<Animal>>, t: Throwable) {}
                                        })
                                    beaconService = BeaconService(this@MainActivity) { zona ->
                                        zonaActual = zona ?: ""
                                    }
                                    beaconService.iniciar()
                                    navController.navigate("mapa") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                navController = navController
                            )
                        }
                        composable("registro") {
                            PantallaRegistro(navController = navController)
                        }
                        composable("mapa") {
                            PantallaMapa(zonaActual = zonaActual, navController = navController)
                        }
                        composable("animales") {
                            PantallaAnimales(navController = navController)
                        }
                        composable("rutas") {
                            PantallaRutas(
                                timestamp = SesionUsuario.rutasTimestamp,
                                zonaActual = zonaActual
                            )
                        }
                        composable("logros") {
                            PantallaLogros()
                        }
                        composable("perfil") {
                            PantallaPerfil(navController = navController)
                        }
                        composable("detalle/{animalId}/{origen}") { backStackEntry ->
                            val animalId = backStackEntry.arguments?.getString("animalId") ?: ""
                            val origen = backStackEntry.arguments?.getString("origen") ?: "animales"
                            PantallaDetalleAnimal(
                                animalId = animalId,
                                origen = origen,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::beaconService.isInitialized) {
            beaconService.detener()
        }
    }
}