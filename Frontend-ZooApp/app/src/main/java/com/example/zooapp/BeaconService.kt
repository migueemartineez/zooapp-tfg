package com.example.zooapp

import android.content.Context
import org.altbeacon.beacon.*

class BeaconService(
    private val context: Context,
    private val onZonaDetectada: (String) -> Unit
) : BeaconConsumer {

    private lateinit var beaconManager: BeaconManager
    private val region = Region("zoo-region", null, null, null)

    fun iniciar() {
        beaconManager = BeaconManager.getInstanceForApplication(context)
        beaconManager.beaconParsers.add(
            BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24") // iBeacon
        )
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
                    Pair(1, 1) -> "zona-africa"
                    Pair(1, 2) -> "zona-madagascar"
                    Pair(2, 1) -> "zona-asia"
                    Pair(2, 2) -> "zona-reptiles"
                    Pair(3, 1) -> "zona-aves"
                    else -> null
                }
                zona?.let { onZonaDetectada(it) }
            }
        }
        try {
            beaconManager.startRangingBeacons(region)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getApplicationContext() = context.applicationContext
    override fun unbindService(connection: android.content.ServiceConnection) =
        context.unbindService(connection)
    override fun bindService(intent: android.content.Intent, connection: android.content.ServiceConnection, flags: Int) =
        context.bindService(intent, connection, flags)
}