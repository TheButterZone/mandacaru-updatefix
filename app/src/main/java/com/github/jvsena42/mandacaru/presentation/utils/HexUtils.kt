package com.github.jvsena42.mandacaru.presentation.utils

object HexUtils {
    private const val HEX_ALPHABET = "0123456789abcdef"
    private const val NIBBLE_BITS = 4
    private const val LOW_NIBBLE_MASK = 0x0F
    private const val BYTE_MASK = 0xFF
    private const val HEX_CHARS_PER_BYTE = 2
    private const val DECIMAL_DIGIT_OFFSET = 10

    fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * HEX_CHARS_PER_BYTE)
        for (b in bytes) {
            val v = b.toInt() and BYTE_MASK
            sb.append(HEX_ALPHABET[v ushr NIBBLE_BITS])
            sb.append(HEX_ALPHABET[v and LOW_NIBBLE_MASK])
        }
        return sb.toString()
    }

    fun hexToBytes(hex: String): ByteArray {
        require(hex.length % HEX_CHARS_PER_BYTE == 0) { "Hex string has odd length" }
        val out = ByteArray(hex.length / HEX_CHARS_PER_BYTE)
        for (i in out.indices) {
            val hi = hexDigit(hex[i * HEX_CHARS_PER_BYTE])
            val lo = hexDigit(hex[i * HEX_CHARS_PER_BYTE + 1])
            out[i] = ((hi shl NIBBLE_BITS) or lo).toByte()
        }
        return out
    }

    private fun hexDigit(c: Char): Int = when (c) {
        in '0'..'9' -> c - '0'
        in 'a'..'f' -> DECIMAL_DIGIT_OFFSET + (c - 'a')
        in 'A'..'F' -> DECIMAL_DIGIT_OFFSET + (c - 'A')
        else -> throw IllegalArgumentException("Invalid hex digit: '$c'")
    }
}
