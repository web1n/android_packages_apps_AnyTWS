package com.example.anytws.utils

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanRecord
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.anytws.models.Model.DeviceBattery

object BluetoothUtils {

    private val TAG = BluetoothUtils::class.java.simpleName
    private const val DEBUG = true

    private val SCANRECORD_FUNC_PARSE_FROM_BYTES =
        ScanRecord::class.java.getMethod("parseFromBytes", ByteArray::class.java)

    fun parseFromBytes(bytes: ByteArray): ScanRecord? {
        return SCANRECORD_FUNC_PARSE_FROM_BYTES
            .runCatching { invoke(null, bytes) as ScanRecord? }
            .onFailure { Log.w(TAG, "Failed to parse scan record", it) }
            .getOrNull()
    }

    fun Context.getBluetoothAdapter(): BluetoothAdapter {
        return getSystemService(BluetoothManager::class.java).adapter
    }

    fun BluetoothDevice.setMetadata(key: Int, value: Any): Boolean {
        val valueStr = value.toString()
        if (valueStr.length > BluetoothDevice.METADATA_MAX_LENGTH) {
            return false
        }

        return setMetadata(key, valueStr.toByteArray())
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_PRIVILEGED)
    fun updateDeviceBatteryMetadata(device: BluetoothDevice, battery: DeviceBattery?) {
        mapOf(
            BluetoothDevice.METADATA_DEVICE_TYPE to BluetoothDevice.DEVICE_TYPE_UNTETHERED_HEADSET,
            BluetoothDevice.METADATA_IS_UNTETHERED_HEADSET to true,
            BluetoothDevice.METADATA_UNTETHERED_LEFT_CHARGING to battery?.left?.charging,
            BluetoothDevice.METADATA_UNTETHERED_RIGHT_CHARGING to battery?.right?.charging,
            BluetoothDevice.METADATA_UNTETHERED_CASE_CHARGING to battery?.case?.charging,
            BluetoothDevice.METADATA_UNTETHERED_LEFT_BATTERY to battery?.left?.battery,
            BluetoothDevice.METADATA_UNTETHERED_RIGHT_BATTERY to battery?.right?.battery,
            BluetoothDevice.METADATA_UNTETHERED_CASE_BATTERY to battery?.case?.battery,
        ).forEach { (key, value) ->
            device.setMetadata(key, value ?: "")
        }
    }

}
