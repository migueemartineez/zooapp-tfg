package com.example.zooapp.service

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import org.altbeacon.beacon.BeaconConsumer
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region

class BeaconService(
    private val context: Context,
    private val onZonaDetectada: (String?) -> Unit
) : BeaconConsumer {

    private lateinit var beaconManager: BeaconManager
    private val region = Region("zoo-region", null, null, null)
    private var ultimaDeteccion: Long = 0
    private val timeWindowMillis = 3000L // Tiempo en ms

    fun iniciar() {
        beaconManager = BeaconManager.getInstanceForApplication(context)

        if (beaconManager.beaconParsers.isEmpty()) {
            beaconManager.beaconParsers.add(
                BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
            )
            beaconManager.setEnableScheduledScanJobs(false)
            beaconManager.setBackgroundMode(false)
            beaconManager.foregroundScanPeriod = 1100L
            beaconManager.foregroundBetweenScanPeriod = 0L
        }

        beaconManager.bind(this)
    }

    fun detener() {
        beaconManager.unbind(this)
    }

    override fun onBeaconServiceConnect() {
        beaconManager.addRangeNotifier { beacons, _ ->
            if (beacons.isNotEmpty()) {
                val beacon = beacons.first()
                val major = beacon.id2.toInt()
                val minor = beacon.id3.toInt()
                val zona = when (Pair(major, minor)) {
                    Pair(1, 1) -> "Isla de Madagascar"
                    Pair(1, 2) -> "África Ecuatorial"
                    Pair(2, 1) -> "Sudeste Asiático"
                    Pair(2, 2) -> "Indo Pacífico"
                    Pair(3, 1) -> "Centro y Sudamérica"
                    else -> null
                }
                ultimaDeteccion = System.currentTimeMillis()
                onZonaDetectada(zona)
            } else {
                val ahora = System.currentTimeMillis()
                if (ahora - ultimaDeteccion > timeWindowMillis) {
                    onZonaDetectada(null)
                }
            }
        }
        try {
            beaconManager.startRangingBeacons(region)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getApplicationContext(): Context = context.applicationContext
    override fun unbindService(connection: ServiceConnection) =
        context.unbindService(connection)
    override fun bindService(intent: Intent, connection: ServiceConnection, flags: Int) =
        context.bindService(intent, connection, flags)
}