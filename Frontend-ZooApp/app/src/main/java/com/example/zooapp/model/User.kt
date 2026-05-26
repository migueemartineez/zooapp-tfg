package com.example.zooapp.model
import com.google.gson.annotations.SerializedName

data class Preferencias(
    val tipoAnimal: List<String> = emptyList(),
    val interesTematico: List<String> = emptyList()
)

data class User(
    @SerializedName("_id")
    val id: String? = null,
    val nombre: String,
    val email: String,
    val password: String,
    val preferencias: Preferencias = Preferencias(),
    val historialVisitas: List<Map<String, Any>> = emptyList(),
    val fechaRegistro: String? = null
)