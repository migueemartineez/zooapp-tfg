package com.example.zooapp
data class User(
    val id: String? = null, // <-- corresponde a _id de MongoDB
    val nombre: String,
    val edad: Int,
    val preferencias: List<String>
)
