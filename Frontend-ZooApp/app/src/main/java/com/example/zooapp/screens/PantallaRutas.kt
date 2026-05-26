package com.example.zooapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    val animalesVistosCuenta: Int = 0,
    val totalAnimalesFavoritos: Int = 0
)

val zonasInfo = listOf(
    ZonaRuta("6a00b55716e824e0e606c8f5", "Isla de Madagascar", 1),
    ZonaRuta("6a00b68916e824e0e606c8f7", "África Ecuatorial", 2),
    ZonaRuta("6a00b7c516e824e0e606c8f9", "Sudeste Asiático", 3),
    ZonaRuta("6a00b88a16e824e0e606c8fb", "Indo Pacífico", 4),
    ZonaRuta("6a00b8d716e824e0e606c8fd", "Centro y Sudamérica", 5)
)

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

    // Animales vistos individualmente (solo con beacon)
    val animalesVistos = usuario?.historialVisitas?.flatMap { visita ->
        (visita["animalesVistos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    }?.toSet() ?: emptySet()

    if (tiposFavoritos.isEmpty()) return emptyList()

    val tiposReales = tiposFavoritos.flatMap { mapearTipos(it) }

    val zonasConPuntuacion = zonasInfo.map { zona ->
        val animalesZona = animales.filter { it.zonaId == zona.id }
        var puntuacion = 0

        val animalesFavoritos = animalesZona.filter { animal ->
            tiposReales.any { it.equals(animal.tipoAnimal, ignoreCase = true) }
        }

        val animalesFavoritosNoVistos = animalesFavoritos.filter { it.nombre !in animalesVistos }
        val animalesFavoritosVistos = animalesFavoritos.count { it.nombre in animalesVistos }

        // Base: puntos por animales favoritos no vistos
        puntuacion += animalesFavoritosNoVistos.size * 3

        // Bonus por especies en peligro no vistas
        if (interesaEspeciesEnPeligro) {
            puntuacion += animalesFavoritosNoVistos.count { it.gradoAmenaza.contains("crítico", ignoreCase = true) } * 2
            puntuacion += animalesFavoritosNoVistos.count { it.gradoAmenaza.equals("En peligro", ignoreCase = true) } * 1
        }

        // Penalización por animales ya vistos en esta zona
        puntuacion -= animalesFavoritosVistos * 2

        zona.copy(
            animales = animalesZona,
            puntuacion = puntuacion,
            animalesVistosCuenta = animalesFavoritosVistos,
            totalAnimalesFavoritos = animalesFavoritos.size
        )
    }

    // Filtrar zonas que tengan al menos un animal favorito no visto
    val zonasFiltradas = zonasConPuntuacion.filter { zona ->
        val animalesFavoritosNoVistos = zona.animales.filter { animal ->
            tiposReales.any { it.equals(animal.tipoAnimal, ignoreCase = true) } && animal.nombre !in animalesVistos
        }
        animalesFavoritosNoVistos.isNotEmpty()
    }

    return if (zonaActual.isNotEmpty()) {
        val ordenActual = zonasInfo.find { it.nombre == zonaActual }?.orden ?: 0

        // Zona actual va primero si tiene animales no vistos
        val zonaActualEnRuta = zonasFiltradas.find { it.nombre == zonaActual }
        val zonasSinActual = zonasFiltradas.filter { it.nombre != zonaActual }

        val zonasAdelante = zonasSinActual.filter { it.orden > ordenActual }
            .sortedWith(compareByDescending<ZonaRuta> { it.puntuacion }.thenBy { it.orden })
        val zonasAtras = zonasSinActual.filter { it.orden < ordenActual }
            .sortedWith(compareByDescending<ZonaRuta> { it.puntuacion }.thenBy { it.orden })

        val rutaSinActual = zonasAdelante + zonasAtras
        if (zonaActualEnRuta != null) listOf(zonaActualEnRuta) + rutaSinActual else rutaSinActual
    } else {
        zonasFiltradas.sortedWith(compareByDescending<ZonaRuta> { it.puntuacion }.thenBy { it.orden })
    }
}

@Composable
fun PantallaRutas(timestamp: Long, zonaActual: String) {
    var animales by remember { mutableStateOf<List<Animal>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var rutaOrdenada by remember { mutableStateOf<List<ZonaRuta>>(emptyList()) }
    var usuario by remember { mutableStateOf(SesionUsuario.usuario) }

    val animalesVistos = remember(usuario) {
        usuario?.historialVisitas?.flatMap { visita ->
            (visita["animalesVistos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        }?.toSet() ?: emptySet()
    }

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
                text = "📡 Estás en: $zonaActual",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
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
        } else if (rutaOrdenada.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎉 ¡Has visto todos los animales de tus zonas favoritas!",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(rutaOrdenada) { index, zona ->
                    val esZonaActual = zona.nombre == zonaActual

                    // Animales favoritos no vistos de esta zona
                    val animalesNoVistos = zona.animales.filter { animal ->
                        tiposReales.any { it.equals(animal.tipoAnimal, ignoreCase = true) } &&
                                animal.nombre !in animalesVistos
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        colors = if (esZonaActual) CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) else CardDefaults.cardColors()
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = zona.nombre,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (zona.totalAnimalesFavoritos > 0) {
                                        Text(
                                            text = "${zona.animalesVistosCuenta}/${zona.totalAnimalesFavoritos} vistos",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (zona.animalesVistosCuenta > 0)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                if (esZonaActual) {
                                    Text(
                                        text = "📡 Zona activa",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                animalesNoVistos.forEach { animal ->
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