package com.example.zooapp

import com.example.zooapp.model.Animal
import com.example.zooapp.model.User

object SesionUsuario {
    var usuario: User? = null
    var ultimaZonaGuardada: String = ""
    var beaconIniciado: Boolean = false
    var rutasTimestamp: Long = 0L
    var todosLosAnimales: List<Animal>? = null
}