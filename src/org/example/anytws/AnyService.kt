package com.example.anytws

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.util.Log
import android.bluetooth.BluetoothDevice
import com.example.anytws.models.Model
import com.example.anytws.models.Model.DeviceBattery
import com.example.anytws.utils.BluetoothUtils.updateDeviceBatteryMetadata

@SuppressLint("MissingPermission")
class AnyService : Service() {

    private val headsetManager: HeadsetManager by lazy { HeadsetManager.getInstance(this) }

    private val batteryListener = object : HeadsetManager.BatteryListener {
        override fun onBatteryChanged(device: BluetoothDevice, model: Model, battery: DeviceBattery) =
            updateBattery(device, model, battery)
    }

    override fun onCreate() {
        super.onCreate()
        if (DEBUG) Log.d(TAG, "onCreate")

        headsetManager.registerListener()
        headsetManager.registerDeviceListener(batteryListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (DEBUG) Log.d(TAG, "onDestroy")

        headsetManager.unregisterDeviceListener(batteryListener)
        headsetManager.unregisterListener()
    }

    override fun onBind(intent: Intent) = null

    private fun updateBattery(device: BluetoothDevice, model: Model, battery: DeviceBattery) {
        if (DEBUG) Log.d(TAG, "updateBattery: device=${device.address}, model=$model, battery=$battery")

        updateDeviceBatteryMetadata(device, battery)
    }

    companion object {
        private val TAG = AnyService::class.java.simpleName
        private const val DEBUG = true
    }
}
