package com.example.zooapp

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.zooapp.ui.theme.ZooAppTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.ui.graphics.Color

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
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PantallaMapa(zonaActual: String, navController: NavController) {

    var animales by remember { mutableStateOf<List<Animal>>(emptyList()) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    // Cuando cambia la zona por beacon
    LaunchedEffect(zonaActual) {
        if (zonaActual.isNotEmpty()) {
            val zonaId = zonaIds[zonaActual]
            if (zonaId != null) {
                RetrofitClient.instance.obtenerAnimalesPorZona(zonaId)
                    .enqueue(object : Callback<List<Animal>> {
                        override fun onResponse(
                            call: Call<List<Animal>>,
                            response: Response<List<Animal>>
                        ) {
                            animales = response.body() ?: emptyList()

                            // Guardar visita
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

                            // Notificar zona activa al WebView
                            webView?.post {
                                webView?.evaluateJavascript(
                                    "window.setZonaActiva('${zonaActual.replace("'", "\\'")}');",
                                    null
                                )
                            }
                        }
                        override fun onFailure(call: Call<List<Animal>>, t: Throwable) {
                            animales = emptyList()
                        }
                    })
            }
        } else {
            animales = emptyList()
            webView?.post {
                webView?.evaluateJavascript("window.setZonaActiva('');", null)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.parseColor("#F5F1E8"))
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            if (zonaActual.isNotEmpty()) {
                                view?.evaluateJavascript(
                                    "window.setZonaActiva('${zonaActual.replace("'", "\\'")}');",
                                    null
                                )
                            }
                        }
                    }

                    addJavascriptInterface(
                        object {
                            @JavascriptInterface
                            fun verDetalleAnimal(nombreAnimal: String) {
                                val animal = animales.find { it.nombre == nombreAnimal }
                                    ?: SesionUsuario.todosLosAnimales?.find { it.nombre == nombreAnimal }
                                animal?.let {
                                    (context as? android.app.Activity)?.runOnUiThread {
                                        navController.navigate("detalle/${it._id}")
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun mapaListo() {}
                        },
                        "AndroidBridge"
                    )

                    loadUrl("file:///android_asset/bioparc_map.html")
                    webView = this
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                webView = view
            }
        )
    }
}