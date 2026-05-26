package com.example.zooapp.model

import com.google.gson.annotations.SerializedName

data class Animal(
    @SerializedName("_id")
    val id: String? = null,
    val nombre: String,
    val descripcion: String,
    val gradoAmenaza: String = "",
    val dieta: String = "",
    val curiosidades: String = "",
    val vida: String = "",
    val ecosistema: String,
    val tipoAnimal: String,
    val imagen: String? = null,
    val zonaId: String
)