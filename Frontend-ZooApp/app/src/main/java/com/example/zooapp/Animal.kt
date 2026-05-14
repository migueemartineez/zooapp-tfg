package com.example.zooapp

data class Animal(
    val _id: String? = null,
    val nombre: String,
    val descripcion: String,
    val gradoAmenaza: String = "",
    val dieta: String = "",
    val curiosidades: String = "",
    val vida: String = "",
    val ecosistema: String,
    val tipoAnimal: String,
    val interesTematico: List<String> = emptyList(),
    val imagen: String? = null,
    val zonaId: String
)