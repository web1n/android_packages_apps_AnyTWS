package com.example.anytws

import android.bluetooth.BluetoothDevice
import com.example.anytws.models.Model.DeviceBattery

import java.util.concurrent.ConcurrentHashMap

object DeviceStatus {

    private val statusMaps = ConcurrentHashMap<Class<*>, ConcurrentHashMap<String, Any>>()

    init {
        registerType<DeviceBattery>()
    }

    private inline fun <reified T : Any> registerType() {
        statusMaps.putIfAbsent(T::class.java, ConcurrentHashMap())
    }

    fun updateStatus(device: BluetoothDevice, value: Any): Boolean {
        val address = device.address

        val map = statusMaps[value.javaClass]
            ?: return true
        return map.put(address, value) != value
    }

    fun <T : Any> getStatus(device: BluetoothDevice, type: Class<T>): T? {
        val address = device.address
        val value = statusMaps[type]!![address]

        @Suppress("UNCHECKED_CAST")
        return value as? T
    }

    inline fun <reified T : Any> getStatus(device: BluetoothDevice): T? {
        return getStatus(device, T::class.java)
    }

    fun clearStatus(device: BluetoothDevice) {
        val address = device.address
        statusMaps.values.forEach { it.remove(address) }
    }

}
