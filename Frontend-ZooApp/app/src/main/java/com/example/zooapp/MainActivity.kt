package com.example.zooapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val usuarios = remember { mutableStateListOf<User>() }

            // Cargar usuarios existentes
            LaunchedEffect(Unit) {
                UserRepository.obtenerUsuarios { lista ->
                    usuarios.clear()
                    usuarios.addAll(lista)
                }
            }

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
                    FormularioUsuario(
                        onAgregar = { nombre, edad, prefs ->
                            val nuevoUsuario = User(nombre = nombre, edad = edad, preferencias = prefs)
                            UserRepository.crearUsuario(nuevoUsuario) { creado ->
                                if (creado != null) {
                                    usuarios.add(creado)
                                    mensaje = "✅ Usuario añadido correctamente"
                                } else {
                                    mensaje = "❌ Error al añadir usuario"
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ListaUsuarios(
                        usuarios = usuarios,
                        onEliminar = { user ->
                            UserRepository.eliminarUsuario(user.id) { exito ->
                                if (exito) {
                                    usuarios.remove(user)
                                    mensaje = "Usuario eliminado correctamente"
                                } else {
                                    mensaje = "Error al eliminar usuario"
                                }
                            }
                        }
                    )
                }
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