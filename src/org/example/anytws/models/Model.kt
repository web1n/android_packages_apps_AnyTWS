package com.example.anytws.models

import android.util.Log

abstract class Model {

    data class DeviceBattery(val left: Battery?, val right: Battery?, val case: Battery?) {
        data class Battery(val battery: Int, val charging: Boolean)
    }

    abstract val companyId: Int
    abstract val atCommandEvent: String

    abstract fun validAtCommandArgs(args: Array<Any>): Boolean
    abstract fun parseAtComandArgs(args: Array<Any>): DeviceBattery

    companion object {

        private const val DEBUG = true
        private val TAG = Model::class.java.simpleName

        val MODELS = listOf(
        )
        val MODEL_COMPANY_IDS = MODELS.map { it.companyId }.toSet()
        val MODEL_AT_COMMAND_EVENTS = MODELS.map { it.atCommandEvent }.toSet()

        fun praseAtCommandArgs(cmd: String, args: Array<Any>): Pair<Model, DeviceBattery>? {
            val model = MODELS.find { it.atCommandEvent == cmd }
            if (model == null) {
                if (DEBUG) Log.d(TAG, "Unknown AT command event: $cmd")
                return null
            }

            if (!model.validAtCommandArgs(args)) {
                if (DEBUG) Log.d(TAG, "Invalid AT command args for ${model::class.java.simpleName}: ${args.joinToString()}")
                return null
            }

            val battery = model.parseAtComandArgs(args)
            if (battery.left == null && battery.right == null && battery.case == null) {
                if (DEBUG) Log.d(TAG, "Invalid battery values for event: $cmd with args: ${args.joinToString()}")
                return null
            }

            return model to battery
        }
    }
}
