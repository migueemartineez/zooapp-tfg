package com.example.zooapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Datos de cada zona con su orden físico
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

@Composable
fun PantallaRutas(navController: NavController) {
    var animales by remember { mutableStateOf<List<Animal>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var rutaOrdenada by remember { mutableStateOf<List<ZonaRuta>>(emptyList()) }

    val usuario = SesionUsuario.usuario
    val tipoAnimalFavorito = usuario?.preferencias?.tipoAnimal?.firstOrNull() ?: ""
    val interestaEspeciesEnPeligro = usuario?.preferencias?.interesTematico?.contains("Especies en peligro") ?: false

    // Zonas ya visitadas por el usuario
    val zonasVisitadas = usuario?.historialVisitas?.flatMap { visita ->
        (visita["zonasVisitadas"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    }?.toSet() ?: emptySet()

    // Zona actual detectada por beacon
    val zonaActual = SesionUsuario.ultimaZonaGuardada

    // Cargar todos los animales
    LaunchedEffect(Unit) {
        RetrofitClient.instance.obtenerAnimales()
            .enqueue(object : Callback<List<Animal>> {
                override fun onResponse(call: Call<List<Animal>>, response: Response<List<Animal>>) {
                    animales = response.body() ?: emptyList()
                    cargando = false

                    // Calcular puntuación de cada zona
                    val zonasConPuntuacion = zonasInfo.map { zona ->
                        val animalesZona = animales.filter { it.zonaId == zona.id }
                        val yaVisitada = zona.nombre in zonasVisitadas

                        var puntuacion = 0

                        // +3 si hay animales del tipo favorito
                        val tipoFavoritoMapeado = when (tipoAnimalFavorito) {
                            "Mamíferos" -> "mamífero"
                            "Reptiles" -> "reptil"
                            "Aves" -> "ave"
                            "Animales acuáticos" -> listOf("pez", "anfibio")
                            else -> ""
                        }
                        val tieneAnimalFavorito = if (tipoFavoritoMapeado is List<*>) {
                            animalesZona.any { it.tipoAnimal in (tipoFavoritoMapeado as List<String>) }
                        } else {
                            animalesZona.any { it.tipoAnimal.equals(tipoFavoritoMapeado as String, ignoreCase = true) }
                        }
                        if (tieneAnimalFavorito) puntuacion += 3

                        // Puntos por especies en peligro
                        if (interestaEspeciesEnPeligro) {
                            puntuacion += animalesZona.count { it.gradoAmenaza.contains("crítico", ignoreCase = true) } * 2
                            puntuacion += animalesZona.count { it.gradoAmenaza.equals("En peligro", ignoreCase = true) } * 1
                        }

                        // -5 si ya visitó la zona
                        if (yaVisitada) puntuacion -= 5

                        zona.copy(
                            animales = animalesZona,
                            puntuacion = puntuacion,
                            yaVisitada = yaVisitada
                        )
                    }

                    // Ordenar: si hay beacon, empezar desde zona actual
                    val rutaFinal = if (zonaActual.isNotEmpty()) {
                        val zonaActualIndex = zonasConPuntuacion.indexOfFirst { it.nombre == zonaActual }
                        if (zonaActualIndex >= 0) {
                            // Reordenar empezando desde la zona actual respetando orden físico
                            val desde = zonasConPuntuacion.subList(zonaActualIndex, zonasConPuntuacion.size)
                            val antes = zonasConPuntuacion.subList(0, zonaActualIndex)
                            (desde + antes).sortedWith(compareByDescending<ZonaRuta> { it.puntuacion }.thenBy { it.orden })
                        } else {
                            zonasConPuntuacion.sortedWith(compareByDescending<ZonaRuta> { it.puntuacion }.thenBy { it.orden })
                        }
                    } else {
                        zonasConPuntuacion.sortedWith(compareByDescending<ZonaRuta> { it.puntuacion }.thenBy { it.orden })
                    }

                    rutaOrdenada = rutaFinal
                }
                override fun onFailure(call: Call<List<Animal>>, t: Throwable) {
                    cargando = false
                }
            })
    }

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

        if (usuario?.preferencias?.tipoAnimal?.isEmpty() == true) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "💡 Configura tus preferencias en el perfil para obtener una ruta más personalizada",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (cargando) {
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
                            // Número de orden
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

                                // Animales destacados según preferencias
                                val animalesDestacados = if (tipoAnimalFavorito.isNotEmpty()) {
                                    val tipoMapeado = when (tipoAnimalFavorito) {
                                        "Mamíferos" -> "mamífero"
                                        "Reptiles" -> "reptil"
                                        "Aves" -> "ave"
                                        "Animales acuáticos" -> "pez"
                                        else -> ""
                                    }
                                    zona.animales.filter { it.tipoAnimal.equals(tipoMapeado, ignoreCase = true) }
                                        .take(2)
                                } else {
                                    zona.animales.take(2)
                                }

                                animalesDestacados.forEach { animal ->
                                    Text(
                                        text = "• ${animal.nombre}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                // Razón de la recomendación
                                Spacer(modifier = Modifier.height(4.dp))
                                val razon = when {
                                    zona.yaVisitada -> "Ya visitaste esta zona"
                                    tieneAnimalFavoritoEnZona(zona, tipoAnimalFavorito) && interestaEspeciesEnPeligro ->
                                        "Tiene tus animales favoritos y especies en peligro"
                                    tieneAnimalFavoritoEnZona(zona, tipoAnimalFavorito) ->
                                        "Tiene tus animales favoritos"
                                    interestaEspeciesEnPeligro && zona.animales.any { it.gradoAmenaza.contains("peligro", ignoreCase = true) } ->
                                        "Tiene especies en peligro"
                                    else -> "Parte del recorrido completo del zoo"
                                }
                                Text(
                                    text = razon,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun tieneAnimalFavoritoEnZona(zona: ZonaRuta, tipoAnimalFavorito: String): Boolean {
    val tipoMapeado = when (tipoAnimalFavorito) {
        "Mamíferos" -> "mamífero"
        "Reptiles" -> "reptil"
        "Aves" -> "ave"
        "Animales acuáticos" -> "pez"
        else -> return false
    }
    return zona.animales.any { it.tipoAnimal.equals(tipoMapeado, ignoreCase = true) }
}