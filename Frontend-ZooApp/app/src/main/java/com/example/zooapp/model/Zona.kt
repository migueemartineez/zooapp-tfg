package com.example.zooapp.model
import com.google.gson.annotations.SerializedName

data class Coordenadas(
    val latitud: Double,
    val longitud: Double,
    val radio: Int
)

data class Zona(
    @SerializedName("_id")
    val id: String? = null,
    val nombre: String,
    val descripcion: String,
    val coordenadas: Coordenadas
)