package com.example.anytws.models

import com.example.anytws.models.Model.DeviceBattery.Battery

object OPPO : Model() {

    private const val KEY_BATTERY_LEFT = 1
    private const val KEY_BATTERY_RIGHT = 2
    private const val KEY_BATTERY_CASE = 3

    override val companyId = 0x079A
    override val atCommandEvent = "+VDBTY"

    override fun validAtCommandArgs(args: Array<Any>): Boolean {
        return args.size >= 1 && (args.size - 1) % 2 == 0 && args.all { it is Int }
    }

    override fun parseAtComandArgs(args: Array<Any>): DeviceBattery {
        var left: Battery? = null
        var right: Battery? = null
        var case: Battery? = null

        val pairCount = args[0] as Int

        for (i in 1..pairCount * 2 step 2) {
            val keyIndex = i
            val valueIndex = i + 1

            if (valueIndex >= args.size) break

            val key = args[keyIndex] as Int
            val value = args[valueIndex] as Int

            val batteryLevel = (value + 1) * 10

            when (key) {
                KEY_BATTERY_LEFT -> left = Battery(batteryLevel, false)
                KEY_BATTERY_RIGHT -> right = Battery(batteryLevel, false)
                KEY_BATTERY_CASE -> case = Battery(batteryLevel, false)
                else -> throw IllegalArgumentException("Unknown battery key: $key in OPPO battery command")
            }
        }

        return DeviceBattery(left, right, case)
    }

}
