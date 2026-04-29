package com.example.anytws

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT
import android.bluetooth.BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD
import android.bluetooth.BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_ARGS
import android.bluetooth.BluetoothHeadset.VENDOR_SPECIFIC_HEADSET_EVENT_COMPANY_ID_CATEGORY
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.example.anytws.models.Model
import com.example.anytws.models.Model.DeviceBattery

import java.util.concurrent.ConcurrentHashMap

class HeadsetManager private constructor(private val context: Context) {

    interface BatteryListener {
        fun onBatteryChanged(device: BluetoothDevice, model: Model, battery: DeviceBattery)
    }

    private val adapter = context.getSystemService(BluetoothManager::class.java).adapter
    private val deviceListeners = ConcurrentHashMap.newKeySet<BatteryListener>()
    private var isAtCommandReceiverRegistered = false
    private var isDeviceReceiverRegistered = false

    private val bluetoothStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val device = intent.getParcelableExtra(
                BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java
            ) ?: return
            if (intent.action != BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED) return

            val state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, -1)
            if (state == BluetoothHeadset.STATE_DISCONNECTED) {
                handleDeviceDisconnected(device)
            }
        }
    }

    private val atCommandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val device = intent?.getParcelableExtra(
                BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java
            ) ?: return
            val cmd = intent.getStringExtra(
                EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD
            ) ?: return
            val args = intent.getSerializableExtra(
                EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_ARGS, Array<Any>::class.java
            ) ?: return

            handleATCommand(device, cmd, args)
        }
    }

    private fun handleDeviceDisconnected(device: BluetoothDevice) {
        Log.d(TAG, "Device disconnected: ${device.address}")
        DeviceStatus.clearStatus(device)
    }

    fun registerListener() {
        synchronized(this) {
            registerAtCommandReceiver()
        }
    }

    fun unregisterListener() {
        synchronized(this) {
            unregisterAtCommandReceiver()
        }
    }

    private fun registerAtCommandReceiver() {
        if (isAtCommandReceiverRegistered) return
        if (DEBUG) Log.d(TAG, "registerAtCommandReceiver")

        val atCommandFilter = IntentFilter(ACTION_VENDOR_SPECIFIC_HEADSET_EVENT)
        Model.MODEL_COMPANY_IDS.forEach { companyId ->
            if (DEBUG) Log.d(TAG, "Registering AT command category for companyId: $companyId")
            atCommandFilter.addCategory("$VENDOR_SPECIFIC_HEADSET_EVENT_COMPANY_ID_CATEGORY.$companyId")
        }

        context.registerReceiver(atCommandReceiver, atCommandFilter, Context.RECEIVER_EXPORTED)
        isAtCommandReceiverRegistered = true
    }

    private fun unregisterAtCommandReceiver() {
        if (!isAtCommandReceiverRegistered) return
        if (DEBUG) Log.d(TAG, "unregisterAtCommandReceiver")

        context.unregisterReceiver(atCommandReceiver)
        isAtCommandReceiverRegistered = false
    }

    fun registerDeviceReceiver() {
        if (isDeviceReceiverRegistered) return
        if (DEBUG) Log.d(TAG, "registerDeviceReceiver")

        val filter = IntentFilter().apply {
            addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
        }

        context.registerReceiver(bluetoothStateReceiver, filter, Context.RECEIVER_EXPORTED)
        isDeviceReceiverRegistered = true
    }

    fun unregisterDeviceReceiver() {
        if (!isDeviceReceiverRegistered) return
        if (DEBUG) Log.d(TAG, "unregisterDeviceReceiver")

        context.unregisterReceiver(bluetoothStateReceiver)
        isDeviceReceiverRegistered = false
    }

    fun registerDeviceListener(listener: BatteryListener) {
        deviceListeners.add(listener)
    }

    fun unregisterDeviceListener(listener: BatteryListener) {
        deviceListeners.remove(listener)
    }

    private fun handleATCommand(device: BluetoothDevice, cmd: String, args: Array<Any>) {
        if (DEBUG) Log.d(TAG, "handleATCommand: ${device.address}, ${cmd}, ${args.joinToString()}")

        if (cmd !in Model.MODEL_AT_COMMAND_EVENTS) {
            Log.w(TAG, "Unknown AT Command event: $cmd")
            return
        }

        val (model, battery) = Model
            .runCatching { praseAtCommandArgs(cmd, args) }
            .onFailure { Log.w(TAG, "Failed to parse AT Command args: ${args.joinToString()}", it) }
            .getOrNull() ?: return
        if (DEBUG) Log.d(TAG, "Parsed AT Command: $device, $battery")

        notifyDeviceBattery(device, model, battery)
    }

    private fun notifyDeviceBattery(device: BluetoothDevice, model: Model, battery: DeviceBattery) {
        if (!DeviceStatus.updateStatus(device, battery)) return

        deviceListeners.forEach { listener ->
            listener.runCatching { onBatteryChanged(device, model, battery) }
                .onFailure { Log.e(TAG, "Error notifying listener", it) }
        }
    }

    companion object {
        private val TAG = HeadsetManager::class.java.simpleName
        const val DEBUG = true

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: HeadsetManager? = null

        fun getInstance(context: Context) = instance ?: synchronized(this) {
            instance ?: HeadsetManager(context.applicationContext).also {
                instance = it
            }
        }
    }
}
