package com.example.zooapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.zooapp.SesionUsuario
import com.example.zooapp.model.Preferencias
import com.example.zooapp.model.User
import com.example.zooapp.network.PreferenciasRequest
import com.example.zooapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Agrupa el historial por día y devuelve un mapa fecha → zonas únicas
private fun agruparHistorialPorDia(
    historial: List<Map<String, Any>>
): Map<String, Set<String>> {
    val mapa = mutableMapOf<String, MutableSet<String>>()
    historial.forEach { visita ->
        val fecha = visita["fecha"]?.toString()?.take(10) ?: return@forEach
        val zonas = (visita["zonasVisitadas"] as? List<*>)
            ?.filterIsInstance<String>()
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
        if (!mapa.containsKey(fecha)) {
            mapa[fecha] = mutableSetOf()
        }
        mapa[fecha]!!.addAll(zonas)
    }
    return mapa.toSortedMap(reverseOrder())
}

// Convierte "2026-05-15" en día, mes y año por separado
private fun parsearFecha(fecha: String): Triple<String, String, String> {
    val partes = fecha.split("-")
    if (partes.size != 3) return Triple("", "", "")
    val anyo = partes[0]
    val mes = when (partes[1]) {
        "01" -> "Ene"
        "02" -> "Feb"
        "03" -> "Mar"
        "04" -> "Abr"
        "05" -> "May"
        "06" -> "Jun"
        "07" -> "Jul"
        "08" -> "Ago"
        "09" -> "Sep"
        "10" -> "Oct"
        "11" -> "Nov"
        "12" -> "Dic"
        else -> ""
    }
    val dia = partes[2]
    return Triple(dia, mes, anyo)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(navController: NavController) {
    val usuario = SesionUsuario.usuario
    var usuarioActualizado by remember { mutableStateOf(usuario) }
    var mensajePreferencias by remember { mutableStateOf("") }

    var tiposSeleccionados by remember { mutableStateOf(
        usuario?.preferencias?.tipoAnimal ?: emptyList()
    )}
    var especiesEnPeligro by remember { mutableStateOf(
        usuario?.preferencias?.interesTematico?.contains("Especies en peligro") ?: false
    )}

    val tiposAnimal = listOf("Mamíferos", "Reptiles", "Aves", "Animales acuáticos")

    LaunchedEffect(Unit) {
        val id = usuario?.id
        if (id != null) {
            RetrofitClient.instance.obtenerUsuario(id)
                .enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        val userActualizado = response.body()
                        usuarioActualizado = userActualizado
                        tiposSeleccionados = userActualizado?.preferencias?.tipoAnimal ?: emptyList()
                        especiesEnPeligro = userActualizado?.preferencias?.interesTematico?.contains("Especies en peligro") ?: false
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
                    SesionUsuario.ultimaZonaGuardada = ""
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
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
                    Checkbox(
                        checked = tipo in tiposSeleccionados,
                        onCheckedChange = { checked ->
                            tiposSeleccionados = if (checked) {
                                tiposSeleccionados + tipo
                            } else {
                                tiposSeleccionados - tipo
                            }
                        }
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
                    val id = usuarioActualizado?.id ?: usuario?.id
                    if (id != null) {
                        val preferenciasRequest = PreferenciasRequest(
                            preferencias = Preferencias(
                                ecosistema = emptyList(),
                                tipoAnimal = tiposSeleccionados,
                                interesTematico = if (especiesEnPeligro) listOf("Especies en peligro") else emptyList()
                            )
                        )
                        RetrofitClient.instance.actualizarPreferencias(id, preferenciasRequest)
                            .enqueue(object : Callback<User> {
                                override fun onResponse(call: Call<User>, response: Response<User>) {
                                    if (response.isSuccessful) {
                                        mensajePreferencias = "Preferencias guardadas correctamente"
                                        SesionUsuario.usuario = response.body()
                                        SesionUsuario.rutasTimestamp = System.currentTimeMillis()
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
            val historialPorDia = agruparHistorialPorDia(historial)

            val diasVisitados = historialPorDia.keys.size
            val ultimaVisita = historialPorDia.keys.firstOrNull() ?: "-"
            val (diaUlt, mesUlt, anyoUlt) = if (ultimaVisita != "-") parsearFecha(ultimaVisita) else Triple("-", "", "")
            val ultimaVisitaFormateada = if (ultimaVisita != "-") "$diaUlt $mesUlt $anyoUlt" else "-"

            val animalMasVisto = historial
                .flatMap { visita ->
                    (visita["animalesVistos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key ?: "-"

            Text("Visitas al zoo: $diasVisitados")
            Text("Última visita: $ultimaVisitaFormateada")
            Text("Animal más visitado: $animalMasVisto")

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- HISTORIAL ---
            Text("Historial de visitas", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (historialPorDia.isEmpty()) {
                Text("Aún no has visitado ninguna zona", style = MaterialTheme.typography.bodyMedium)
            } else {
                historialPorDia.forEach { (fecha, zonas) ->
                    val (dia, mes, anyo) = parsearFecha(fecha)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(
                            0.5.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Columna de fecha
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(36.dp)
                            ) {
                                Text(
                                    text = dia,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = mes,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = anyo,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            // Divisor vertical
                            HorizontalDivider(
                                modifier = Modifier
                                    .width(0.5.dp)
                                    .height(48.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            // Columna de info
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Visita al zoo",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (zonas.isNotEmpty()) {
                                    Text(
                                        text = zonas.joinToString(" · "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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