package com.example.anytws.utils

import java.io.ByteArrayInputStream


object ByteUtils {

    fun Byte.isBitSet(bit: Int) = (toInt() shr bit) and 1 != 0

    fun Int.getHighByte(): Byte {
        return (this shr 8).toByte()
    }

    fun Int.getLowByte(): Byte {
        return (this and 0xFF).toByte()
    }

    fun ByteArray.toHexString(separator: String = ""): String {
        return joinToString(separator) { "%02X".format(it) }
    }

    fun String.hexToBytes(): ByteArray {
        require(length % 2 == 0) {
            "Hex string must have an even length"
        }

        val bytes = ByteArray(length / 2)
        for (i in indices step 2) {
            val high = this[i].digitToIntOrNull(16)
            val low = this[i + 1].digitToIntOrNull(16)
            check(high != null && low != null) {
                "Invalid hex character at position $i"
            }

            bytes[i / 2] = (high shl 4 or low).toByte()
        }

        return bytes
    }

}
