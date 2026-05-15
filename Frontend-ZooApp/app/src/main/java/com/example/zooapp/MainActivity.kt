package com.example.zooapp

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.zooapp.ui.theme.ZooAppTheme
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
                                    beaconService = BeaconService(this@MainActivity) { zona ->
                                        zonaActual = zona ?: ""
                                    }
                                    beaconService.iniciar()
                                    navController.navigate("mapa")
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
                            PantallaRutas(navController = navController, timestamp = SesionUsuario.rutasTimestamp)
                        }
                        composable("logros") {
                            PantallaLogros()
                        }
                        composable("perfil") {
                            PantallaPerfil(navController = navController)
                        }
                        composable("detalle/{animalId}") { backStackEntry ->
                            val animalId = backStackEntry.arguments?.getString("animalId") ?: ""
                            PantallaDetalleAnimal(animalId = animalId, navController = navController)
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

@Composable
fun PantallaLogin(onLoginExitoso: (User) -> Unit, navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ZooApp", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                UserRepository.loginUsuario(email, password) { response ->
                    if (response != null) {
                        onLoginExitoso(response.usuario)
                    } else {
                        mensaje = "Email o contraseña incorrectos"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }

        TextButton(
            onClick = { navController.navigate("registro") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("¿No tienes cuenta? Regístrate")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (mensaje.isNotEmpty()) {
            Text(mensaje, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun PantallaMapa(zonaActual: String, navController: NavController) {
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(16.0)
            center(Point.fromLngLat(-4.6342, 36.5397))
            pitch(0.0)
            bearing(0.0)
        }
    }

    // Lista de animales de la zona actual
    var animales by remember { mutableStateOf<List<Animal>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }

    // Cuando cambia la zona, consultar el backend
    LaunchedEffect(zonaActual) {
        if (zonaActual.isNotEmpty()) {
            val zonaId = zonaIds[zonaActual]
            if (zonaId != null) {
                cargando = true
                RetrofitClient.instance.obtenerAnimalesPorZona(zonaId)
                    .enqueue(object : Callback<List<Animal>> {
                        override fun onResponse(
                            call: Call<List<Animal>>,
                            response: Response<List<Animal>>
                        ) {
                            animales = response.body() ?: emptyList()
                            cargando = false

                            // Guardar visita solo si es una zona diferente a la última visitada
                            val usuarioId = SesionUsuario.usuario?._id
                            if (usuarioId != null && animales.isNotEmpty() && zonaActual != SesionUsuario.ultimaZonaGuardada) {
                                SesionUsuario.ultimaZonaGuardada = zonaActual
                                val visita = VisitaRequest(
                                    zonasVisitadas = listOf(zonaActual),
                                    animalesVistos = emptyList()
                                )
                                RetrofitClient.instance.anadirVisita(usuarioId, visita)
                                    .enqueue(object : Callback<User> {
                                        override fun onResponse(call: Call<User>, response: Response<User>) {
                                            response.body()?.let { SesionUsuario.usuario = it }
                                        }
                                        override fun onFailure(call: Call<User>, t: Throwable) {}
                                    })
                            }
                        }
                        override fun onFailure(call: Call<List<Animal>>, t: Throwable) {
                            animales = emptyList()
                            cargando = false
                        }
                    })
            }
        } else {
            animales = emptyList()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState
        )

        if (zonaActual.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth(0.9f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = zonaActual,
                        style = MaterialTheme.typography.titleMedium
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )

                    if (cargando) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else if (animales.isNotEmpty()) {
                        LazyColumn {
                            items(animales) { animal ->
                                TextButton(
                                    onClick = { navController.navigate("detalle/${animal._id}") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "• ${animal.nombre}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}