package com.example.zooapp.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zooapp.SesionUsuario

private data class ZonaLogro(
    val nombre: String,
    val emoji: String
)

private val ZONAS_ORDENADAS = listOf(
    ZonaLogro("Isla de Madagascar", "🐒"),
    ZonaLogro("África Ecuatorial", "🦍"),
    ZonaLogro("Sudeste Asiático", "🐯"),
    ZonaLogro("Indo Pacífico", "🦎"),
    ZonaLogro("Centro y Sudamérica", "🦜")
)

private val ColorVisitado = Color(0xFF1D9E75)
private val ColorVisitadoFondo = Color(0xFFE1F5EE)
private val ColorVisitadoTexto = Color(0xFF0F6E56)
private val ColorNoVisitado = Color(0xFFB4B2A9)
private val ColorNoVisitadoFondo = Color(0xFFF1EFE8)
private val ColorNoVisitadoTexto = Color(0xFF888780)

@Composable
fun PantallaLogros() {
    val zonasVisitadas = remember {
        SesionUsuario.usuario?.historialVisitas?.flatMap { visita ->
            (visita["zonasVisitadas"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        }?.toSet() ?: emptySet()
    }

    val totalAnimales = 19
    val animalesVistos = remember {
        SesionUsuario.usuario?.historialVisitas?.flatMap { visita ->
            (visita["animalesVistos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        }?.toSet() ?: emptySet()
    }
    val animalesCompletados = animalesVistos.size
    val porcentaje = (animalesCompletados * 100f / totalAnimales)
    val zooCompletado = zonasVisitadas.size == ZONAS_ORDENADAS.size

    var progressAnimado by remember { mutableFloatStateOf(0f) }
    val progressAnimadoState by animateFloatAsState(
        targetValue = progressAnimado,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        progressAnimado = porcentaje / 100f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título
        Column {
            Text(
                text = "Logros",
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Sellos desbloqueados al visitar cada zona",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // Tarjeta de progreso
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progreso del zoo",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "$animalesCompletados / $totalAnimales animales",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (zooCompletado) ColorVisitadoTexto else MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progressAnimadoState },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = ColorVisitado,
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${porcentaje.toInt()}% completado",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        // Grid de sellos
        val filas = ZONAS_ORDENADAS.chunked(2)
        filas.forEachIndexed { _, fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                fila.forEach { zona ->
                    val visitada = zona.nombre in zonasVisitadas
                    StampSello(
                        zona = zona,
                        visitada = visitada,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Si la fila tiene un solo elemento (último impar), rellenar el espacio
                if (fila.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Logro especial
        LogroEspecial(desbloqueado = zooCompletado)

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun StampSello(
    zona: ZonaLogro,
    visitada: Boolean,
    modifier: Modifier = Modifier
) {
    val colorBorde = if (visitada) ColorVisitado else ColorNoVisitado
    val colorFondo = if (visitada) ColorVisitadoFondo else ColorNoVisitadoFondo
    val colorTexto = if (visitada) ColorVisitadoTexto else ColorNoVisitadoTexto
    val colorBadgeFondo = if (visitada) ColorVisitadoFondo else ColorNoVisitadoFondo
    val alphaCard = if (visitada) 1f else 0.5f
    val textoBadge = if (visitada) "Visitada" else "No visitada"

    Card(
        modifier = modifier.alpha(alphaCard),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Círculo stamp con rotación
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .rotate(-4f)
                    .background(color = colorFondo, shape = CircleShape)
                    .border(width = 2.dp, color = colorBorde, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = zona.emoji,
                    fontSize = 28.sp
                )
            }

            // Nombre de la zona
            Text(
                text = zona.nombre,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colorTexto,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            // Badge
            Box(
                modifier = Modifier
                    .background(color = colorBadgeFondo, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = textoBadge,
                    fontSize = 10.sp,
                    color = colorTexto,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun LogroEspecial(desbloqueado: Boolean) {
    val alphaCard = if (desbloqueado) 1f else 0.4f
    val borderColor = if (desbloqueado) ColorVisitado else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (desbloqueado) 1.5.dp else 0.5.dp
    val iconoFondo = if (desbloqueado) ColorVisitadoFondo else ColorNoVisitadoFondo

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alphaCard),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = iconoFondo, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🏆", fontSize = 22.sp)
            }
            Column {
                Text(
                    text = "Explorador del zoo",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (desbloqueado)
                        "¡Has recorrido todas las zonas del BioParc!"
                    else
                        "Visita las 5 zonas del BioParc para desbloquear",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}