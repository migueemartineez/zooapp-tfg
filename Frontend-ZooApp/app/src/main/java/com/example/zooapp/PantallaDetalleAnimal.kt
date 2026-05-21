package com.example.zooapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleAnimal(animalId: String, navController: NavController) {
    var animal by remember { mutableStateOf<Animal?>(null) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(animalId) {
        RetrofitClient.instance.obtenerAnimalPorId(animalId)
            .enqueue(object : Callback<Animal> {
                override fun onResponse(call: Call<Animal>, response: Response<Animal>) {
                    animal = response.body()
                    cargando = false

                    // Marcar animal como visto al abrir el detalle
                    val usuarioId = SesionUsuario.usuario?._id
                    val nombreAnimal = animal?.nombre
                    if (usuarioId != null && nombreAnimal != null) {
                        val visita = VisitaRequest(
                            zonasVisitadas = emptyList(),
                            animalesVistos = listOf(nombreAnimal)
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
                override fun onFailure(call: Call<Animal>, t: Throwable) {
                    cargando = false
                }
            })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(animal?.nombre ?: "") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (cargando) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (animal != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                animal?.let { a ->
                    // Imagen del animal
                    if (!a.imagen.isNullOrEmpty()) {
                        AsyncImage(
                            model = a.imagen,
                            contentDescription = a.nombre,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    InfoItem(titulo = "Ecosistema", contenido = a.ecosistema)
                    InfoItem(titulo = "Tipo de animal", contenido = a.tipoAnimal.replaceFirstChar { it.uppercase() })
                    InfoItem(titulo = "Descripción", contenido = a.descripcion)
                    InfoItem(titulo = "Dieta", contenido = a.dieta)
                    InfoItem(titulo = "Curiosidades", contenido = a.curiosidades)
                    InfoItem(titulo = "Grado de amenaza", contenido = a.gradoAmenaza)
                    InfoItem(titulo = "Esperanza de vida", contenido = a.vida)
                }
            }
        }
    }
}

@Composable
fun InfoItem(titulo: String, contenido: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = contenido,
            style = MaterialTheme.typography.bodyMedium
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )
    }
}