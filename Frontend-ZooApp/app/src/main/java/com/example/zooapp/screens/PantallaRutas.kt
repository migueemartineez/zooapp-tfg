package com.example.zooapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.zooapp.SesionUsuario
import com.example.zooapp.model.Animal
import com.example.zooapp.model.User
import com.example.zooapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class ZonaRuta(
    val id: String,
    val nombre: String,
    val orden: Int,
    val animales: List<Animal> = emptyList(),
    val puntuacion: Int = 0,
    val yaVisitada: Boolean = false
)

val zonasInfo = listOf(
    ZonaRuta("6a00b55716e824e0e606c8f5", "Isla de Madagascar", 1),
    ZonaRuta("6a00b68916e824e0e606c8f7", "África Ecuatorial", 2),
    ZonaRuta("6a00b7c516e824e0e606c8f9", "Sudeste Asiático", 3),
    ZonaRuta("6a00b88a16e824e0e606c8fb", "Indo Pacífico", 4),
    ZonaRuta("6a00b8d716e824e0e606c8fd", "Centro y Sudamérica", 5)
)

// Mapeo de tipo favorito a tipos reales en BD
fun mapearTipos(tipoFavorito: String): List<String> {
    return when (tipoFavorito) {
        "Mamíferos" -> listOf("mamífero")
        "Reptiles" -> listOf("reptil")
        "Aves" -> listOf("ave")
        "Animales acuáticos" -> listOf("pez", "anfibio")
        else -> emptyList()
    }
}

fun calcularRuta(
    animales: List<Animal>,
    usuario: User?,
    zonaActual: String
): List<ZonaRuta> {
    val tiposFavoritos = usuario?.preferencias?.tipoAnimal ?: emptyList()
    val interesaEspeciesEnPeligro = usuario?.preferencias?.interesTematico?.contains("Especies en peligro") ?: false
    val zonasVisitadas = usuario?.historialVisitas?.flatMap { visita ->
        (visita["zonasVisitadas"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    }?.toSet() ?: emptySet()

    if (tiposFavoritos.isEmpty()) return emptyList()

    val tiposReales = tiposFavoritos.flatMap { mapearTipos(it) }

    val zonasConPuntuacion = zonasInfo.map { zona ->
        val animalesZona = animales.filter { it.zonaId == zona.id }
        val yaVisitada = zona.nombre in zonasVisitadas
        var puntuacion = 0

        val animalesFavoritos = animalesZona.filter { animal ->
            tiposReales.any { it.equals(animal.tipoAnimal, ignoreCase = true) }
        }

        if (animalesFavoritos.isNotEmpty()) puntuacion += 3

        if (interesaEspeciesEnPeligro) {
            puntuacion += animalesFavoritos.count { it.gradoAmenaza.contains("crítico", ignoreCase = true) } * 2
            puntuacion += animalesFavoritos.count { it.gradoAmenaza.equals("En peligro", ignoreCase = true) } * 1
        }

        if (yaVisitada) puntuacion -= 5

        zona.copy(
            animales = animalesZona,
            puntuacion = puntuacion,
            yaVisitada = yaVisitada
        )
    }

    // Filtrar zonas con animales favoritos y excluir la zona actual
    val zonasFiltradas = zonasConPuntuacion.filter { zona ->
        zona.nombre != zonaActual &&
                zona.animales.any { animal ->
                    tiposReales.any { it.equals(animal.tipoAnimal, ignoreCase = true) }
                }
    }

    return if (zonaActual.isNotEmpty()) {
        val ordenActual = zonasInfo.find { it.nombre == zonaActual }?.orden ?: 0

        // Separar zonas en adelante y atrás según orden físico
        val zonasAdelante = zonasFiltradas.filter { it.orden > ordenActual }
            .sortedWith(compareByDescending<ZonaRuta> { it.puntuacion }.thenBy { it.orden })
        val zonasAtras = zonasFiltradas.filter { it.orden < ordenActual }
            .sortedWith(compareByDescending<ZonaRuta> { it.puntuacion }.thenBy { it.orden })

        zonasAdelante + zonasAtras
    } else {
        zonasFiltradas.sortedWith(compareByDescending<ZonaRuta> { it.puntuacion }.thenBy { it.orden })
    }
}

@Composable
fun PantallaRutas(navController: NavController, timestamp: Long) {
    var animales by remember { mutableStateOf<List<Animal>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var rutaOrdenada by remember { mutableStateOf<List<ZonaRuta>>(emptyList()) }
    var usuario by remember { mutableStateOf(SesionUsuario.usuario) }

    val zonaActual = SesionUsuario.ultimaZonaGuardada

    LaunchedEffect(timestamp) {
        cargando = true
        val id = SesionUsuario.usuario?.id
        if (id != null) {
            RetrofitClient.instance.obtenerUsuario(id)
                .enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        val userActualizado = response.body()
                        SesionUsuario.usuario = userActualizado
                        usuario = userActualizado

                        RetrofitClient.instance.obtenerAnimales()
                            .enqueue(object : Callback<List<Animal>> {
                                override fun onResponse(call: Call<List<Animal>>, response: Response<List<Animal>>) {
                                    animales = response.body() ?: emptyList()
                                    rutaOrdenada = calcularRuta(animales, userActualizado, zonaActual)
                                    cargando = false
                                }
                                override fun onFailure(call: Call<List<Animal>>, t: Throwable) {
                                    cargando = false
                                }
                            })
                    }
                    override fun onFailure(call: Call<User>, t: Throwable) {
                        RetrofitClient.instance.obtenerAnimales()
                            .enqueue(object : Callback<List<Animal>> {
                                override fun onResponse(call: Call<List<Animal>>, response: Response<List<Animal>>) {
                                    animales = response.body() ?: emptyList()
                                    rutaOrdenada = calcularRuta(animales, usuario, zonaActual)
                                    cargando = false
                                }
                                override fun onFailure(call: Call<List<Animal>>, t: Throwable) {
                                    cargando = false
                                }
                            })
                    }
                })
        }
    }

    val tiposFavoritos = usuario?.preferencias?.tipoAnimal ?: emptyList()
    val tiposReales = tiposFavoritos.flatMap { mapearTipos(it) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Mi ruta recomendada",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        if (zonaActual.isNotEmpty()) {
            Text(
                text = "Estás en: $zonaActual",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (tiposFavoritos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 Ve a tu perfil y configura tus preferencias para ver tu ruta personalizada",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(rutaOrdenada) { index, zona ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 16.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = zona.nombre,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (zona.yaVisitada) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "✓ Visitada",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Mostrar solo animales de los tipos favoritos
                                val animalesDestacados = zona.animales.filter { animal ->
                                    tiposReales.any { it.equals(animal.tipoAnimal, ignoreCase = true) }
                                }

                                animalesDestacados.forEach { animal ->
                                    Text(
                                        text = "• ${animal.nombre}",
                                        style = MaterialTheme.typography.bodySmall
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