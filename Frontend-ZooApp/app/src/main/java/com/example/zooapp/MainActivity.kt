package com.example.zooapp

import android.os.Bundle
import android.os.RemoteException
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.altbeacon.beacon.*

class MainActivity : ComponentActivity(), BeaconConsumer {

    private lateinit var beaconManager: BeaconManager
    private var zonaActual by mutableStateOf("Sin zona detectada")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar BeaconManager
        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.add(
            BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24") // iBeacon layout
        )
        beaconManager.bind(this)

        setContent {
            Scaffold { padding ->
                Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                    // Texto para mostrar la zona detectada
                    Text("Zona actual: $zonaActual", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    // ---------------- BEACONS ----------------

    override fun onBeaconServiceConnect() {
        try {
            val region = Region("all-beacons-region", null, null, null)
            beaconManager.addRangeNotifier { beacons, _ ->
                zonaActual = if (beacons.isNotEmpty()) {
                    val b = beacons.first()
                    val major = b.id2.toInt()
                    val minor = b.id3.toInt()
                    when (Pair(major, minor)) {
                        Pair(1,1) -> "Reptiles - Serpientes"
                        Pair(1,2) -> "Reptiles - Lagartos"
                        Pair(2,1) -> "Mamíferos - Leones"
                        Pair(2,2) -> "Mamíferos - Elefantes"
                        Pair(3,1) -> "Aves - Pinguinos"
                        else -> "Zona desconocida"
                    }
                } else {
                    "Sin zona detectada"
                }
            }
            beaconManager.startRangingBeacons(region)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconManager.unbind(this)
    }
}

/*
package com.example.zooapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.RemoteException
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.altbeacon.beacon.*

class MainActivity : ComponentActivity(), BeaconConsumer {

    private lateinit var beaconManager: BeaconManager
    private var zonaActual by mutableStateOf("Sin zona detectada")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1️⃣ Pedir permisos en tiempo de ejecución
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), 1234
            )
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1234)
        }

        // 2️⃣ Inicializar BeaconManager
        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.add(
            BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        )
        beaconManager.bind(this)

        // 3️⃣ UI con Compose
        setContent {
            val usuarios = remember { mutableStateListOf<User>() }
            var mensaje by remember { mutableStateOf("") }
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(mensaje) {
                if (mensaje.isNotEmpty()) {
                    snackbarHostState.showSnackbar(mensaje)
                    mensaje = ""
                }
            }

            Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
                Column(modifier = Modifier.padding(padding).padding(16.dp)) {

                    // Formulario de usuario
                    FormularioUsuario { nombre, edad, prefs ->
                        val nuevoUsuario = User(nombre = nombre, edad = edad, preferencias = prefs)
                        UserRepository.crearUsuario(nuevoUsuario) { creado: User? ->
                            if (creado != null) {
                                usuarios.add(creado)
                                mensaje = "✅ Usuario añadido correctamente"
                            } else {
                                mensaje = "❌ Error al añadir usuario"
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lista de usuarios
                    ListaUsuarios(usuarios = usuarios) { user ->
                        UserRepository.eliminarUsuario(user.id) { exito: Boolean ->
                            if (exito) {
                                usuarios.remove(user)
                                mensaje = "Usuario eliminado correctamente"
                            } else {
                                mensaje = "Error al eliminar usuario"
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Zona beacon detectada
                    Text("Zona actual: $zonaActual", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    // ---------------- COMPOSABLES ----------------

    @Composable
    fun FormularioUsuario(onAgregar: (String, Int, List<String>) -> Unit) {
        var nombre by remember { mutableStateOf("") }
        var edad by remember { mutableStateOf("") }
        var preferencias by remember { mutableStateOf("") }

        Column {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = edad,
                onValueChange = { edad = it },
                label = { Text("Edad") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = preferencias,
                onValueChange = { preferencias = it },
                label = { Text("Preferencias (separadas por coma)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val edadInt = edad.toIntOrNull()
                    val listaPrefs = preferencias.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    if (nombre.isNotBlank() && edadInt != null && edadInt > 0) {
                        onAgregar(nombre, edadInt, listaPrefs)
                        nombre = ""; edad = ""; preferencias = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Añadir usuario") }
        }
    }

    @Composable
    fun ListaUsuarios(usuarios: List<User>, onEliminar: (User) -> Unit) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(usuarios) { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Nombre: ${user.nombre}", style = MaterialTheme.typography.titleMedium)
                        Text("Edad: ${user.edad}", style = MaterialTheme.typography.bodyMedium)
                        Text("Preferencias: ${user.preferencias.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onEliminar(user) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Eliminar") }
                    }
                }
            }
        }
    }

    // ---------------- BEACONS ----------------

    override fun onBeaconServiceConnect() {
        val region = Region("all-beacons-region", null, null, null)
        beaconManager.addRangeNotifier { beacons, _ ->
            zonaActual = if (beacons.isNotEmpty()) {
                val b = beacons.first()
                val major = b.id2.toInt()
                val minor = b.id3.toInt()
                when (Pair(major, minor)) {
                    Pair(1,1) -> "Reptiles - Serpientes"
                    Pair(1,2) -> "Reptiles - Lagartos"
                    Pair(2,1) -> "Mamíferos - Leones"
                    Pair(2,2) -> "Mamíferos - Elefantes"
                    Pair(3,1) -> "Aves - Pinguinos"
                    else -> "Zona desconocida"
                }
            } else {
                "Sin zona detectada"
            }
        }
        try {
            beaconManager.startRangingBeacons(region)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconManager.unbind(this)
    }
}

 */