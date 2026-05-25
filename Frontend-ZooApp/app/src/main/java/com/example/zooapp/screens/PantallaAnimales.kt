package com.example.zooapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.zooapp.model.Animal
import com.example.zooapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun PantallaAnimales(navController: NavController) {
    var animales by remember { mutableStateOf<List<Animal>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var busqueda by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf("Todos") }

    val filtros = listOf("Todos", "Mamíferos", "Reptiles", "Aves", "Animales acuáticos")

    // Cargar todos los animales al entrar
    LaunchedEffect(Unit) {
        RetrofitClient.instance.obtenerAnimales()
            .enqueue(object : Callback<List<Animal>> {
                override fun onResponse(call: Call<List<Animal>>, response: Response<List<Animal>>) {
                    animales = response.body() ?: emptyList()
                    cargando = false
                }
                override fun onFailure(call: Call<List<Animal>>, t: Throwable) {
                    cargando = false
                }
            })
    }

    // Filtrar animales por búsqueda y tipo
    val animalesFiltrados = animales.filter { animal ->
        val coincideBusqueda = animal.nombre.contains(busqueda, ignoreCase = true)
        val coincideFiltro = filtroSeleccionado == "Todos" ||
                when (filtroSeleccionado) {
                    "Mamíferos" -> animal.tipoAnimal.equals("mamífero", ignoreCase = true)
                    "Reptiles" -> animal.tipoAnimal.equals("reptil", ignoreCase = true)
                    "Aves" -> animal.tipoAnimal.equals("ave", ignoreCase = true)
                    "Animales acuáticos" -> animal.tipoAnimal.equals("pez", ignoreCase = true) ||
                            animal.tipoAnimal.equals("anfibio", ignoreCase = true)
                    else -> false
                }
        coincideBusqueda && coincideFiltro
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // Título
        Text(
            text = "Animales",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Buscador
        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            label = { Text("Buscar animal...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Filtros
        ScrollableTabRow(
            selectedTabIndex = filtros.indexOf(filtroSeleccionado),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            filtros.forEach { filtro ->
                Tab(
                    selected = filtroSeleccionado == filtro,
                    onClick = { filtroSeleccionado = filtro },
                    text = { Text(filtro) }
                )
            }
        }

        // Lista de animales
        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (animalesFiltrados.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No se encontraron animales")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(animalesFiltrados) { animal ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        onClick = { navController.navigate("detalle/${animal.id}") }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = animal.nombre,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "${animal.tipoAnimal.replaceFirstChar { it.uppercase() }} · ${animal.ecosistema}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}