package com.example.zooapp

data class Coordenadas(
    val latitud: Double,
    val longitud: Double,
    val radio: Int
)

data class Zona(
    val _id: String? = null,
    val nombre: String,
    val descripcion: String,
    val coordenadas: Coordenadas
)