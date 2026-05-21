package com.example.zooapp

object SesionUsuario {
    var usuario: User? = null
    var ultimaZonaGuardada: String = ""
    var beaconIniciado: Boolean = false
    var rutasTimestamp: Long = 0L
    var todosLosAnimales: List<Animal>? = null
}