package com.example.zooapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(navController: NavController) {
    val usuario = SesionUsuario.usuario
    var usuarioActualizado by remember { mutableStateOf(usuario) }
    var mensajePreferencias by remember { mutableStateOf("") }

    // Preferencias seleccionadas (se cargan desde el usuario guardado)
    var tipoAnimalSeleccionado by remember { mutableStateOf(
        usuario?.preferencias?.tipoAnimal?.firstOrNull() ?: ""
    )}
    var especiesEnPeligro by remember { mutableStateOf(
        usuario?.preferencias?.interesTematico?.contains("Especies en peligro") ?: false
    )}

    // Opciones disponibles
    val tiposAnimal = listOf("Mamíferos", "Reptiles", "Aves", "Animales acuáticos")

    // Cargar usuario actualizado al entrar al perfil
    LaunchedEffect(Unit) {
        val id = usuario?._id
        if (id != null) {
            RetrofitClient.instance.obtenerUsuario(id)
                .enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        val userActualizado = response.body()
                        usuarioActualizado = userActualizado
                        // Actualizar preferencias con los datos del servidor
                        tipoAnimalSeleccionado = userActualizado?.preferencias?.tipoAnimal?.firstOrNull() ?: ""
                        especiesEnPeligro = userActualizado?.preferencias?.interesTematico?.contains("Especies en peligro") ?: false
                        // Actualizar sesión
                        SesionUsuario.usuario = userActualizado
                    }
                    override fun onFailure(call: Call<User>, t: Throwable) {}
                })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // --- DATOS DEL USUARIO ---
            Text("Mis datos", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Nombre: ${usuarioActualizado?.nombre ?: ""}")
            Text("Email: ${usuarioActualizado?.email ?: ""}")

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    SesionUsuario.usuario = null
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- PREFERENCIAS ---
            Text("Mis preferencias", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Tipo de animal favorito", style = MaterialTheme.typography.labelMedium)
            tiposAnimal.forEach { tipo ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = tipoAnimalSeleccionado == tipo,
                        onClick = { tipoAnimalSeleccionado = tipo }
                    )
                    Text(tipo)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("¿Te interesan las especies en peligro?", style = MaterialTheme.typography.labelMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = especiesEnPeligro,
                    onCheckedChange = { especiesEnPeligro = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (especiesEnPeligro) "Sí" else "No")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val id = usuario?._id
                    if (id != null) {
                        val preferencias = Preferencias(
                            ecosistema = emptyList(),
                            tipoAnimal = if (tipoAnimalSeleccionado.isNotEmpty()) listOf(tipoAnimalSeleccionado) else emptyList(),
                            interesTematico = if (especiesEnPeligro) listOf("Especies en peligro") else emptyList()
                        )
                        RetrofitClient.instance.actualizarPreferencias(id, preferencias)
                            .enqueue(object : Callback<User> {
                                override fun onResponse(call: Call<User>, response: Response<User>) {
                                    if (response.isSuccessful) {
                                        mensajePreferencias = "Preferencias guardadas correctamente"
                                        SesionUsuario.usuario = response.body()
                                    }
                                }
                                override fun onFailure(call: Call<User>, t: Throwable) {
                                    mensajePreferencias = "Error al guardar preferencias"
                                }
                            })
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar preferencias")
            }

            if (mensajePreferencias.isNotEmpty()) {
                Text(
                    mensajePreferencias,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- ACTIVIDAD ---
            Text("Mi actividad", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            val historial = usuarioActualizado?.historialVisitas ?: emptyList()
            val zonasUnicas = historial.flatMap { visita ->
                (visita["zonasVisitadas"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            }.toSet()
            val animalesUnicos = historial.flatMap { visita ->
                (visita["animalesVistos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            }.toSet()

            Text("Zonas visitadas: ${zonasUnicas.size} de 5")
            Text("Animales vistos: ${animalesUnicos.size}")
            Text("Visitas totales: ${historial.size}")

            Spacer(modifier = Modifier.height(8.dp))

            // Barra de progreso de zonas
            LinearProgressIndicator(
                progress = { zonasUnicas.size / 5f },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${zonasUnicas.size}/5 zonas completadas",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- HISTORIAL ---
            Text("Historial de visitas", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (historial.isEmpty()) {
                Text("Aún no has visitado ninguna zona", style = MaterialTheme.typography.bodyMedium)
            } else {
                historial.reversed().forEach { visita ->
                    val zonas = (visita["zonasVisitadas"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    val fecha = visita["fecha"]?.toString()?.take(10) ?: ""
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(fecha, style = MaterialTheme.typography.labelMedium)
                            zonas.forEach { zona ->
                                Text("• $zona", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}