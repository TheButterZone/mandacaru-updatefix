package com.github.jvsena42.mandacaru.presentation.utils

/**
 * Client-side validator for the "Add Peer" address field. Accepts:
 *  - IPv4 with optional ":port"        e.g. 189.44.63.101 or 189.44.63.101:8333
 *  - Bracketed IPv6 with optional port e.g. [2001:db8::1] or [2001:db8::1]:8333
 *
 * Hostnames and .onion addresses are intentionally rejected — the Settings
 * hint string only documents IPv4 and bracketed IPv6.
 */
object PeerAddressValidator {

    sealed interface Result {
        object Valid : Result
        object Empty : Result
        object InvalidFormat : Result
        object InvalidIpv4 : Result
        object InvalidIpv6 : Result
        object InvalidPort : Result
    }

    fun validate(input: String): Result {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return Result.Empty

        if (trimmed.startsWith("[")) {
            val close = trimmed.indexOf(']')
            if (close < 0) return Result.InvalidIpv6
            val host = trimmed.substring(1, close)
            val rest = trimmed.substring(close + 1)
            if (!isValidIpv6(host)) return Result.InvalidIpv6
            return validatePortSuffix(rest, Result.InvalidPort)
        }

        val colonCount = trimmed.count { it == ':' }
        if (colonCount > 1) return Result.InvalidFormat

        val (host, portPart) = if (colonCount == 1) {
            val idx = trimmed.indexOf(':')
            trimmed.substring(0, idx) to trimmed.substring(idx)
        } else {
            trimmed to ""
        }

        if (!isValidIpv4(host)) return Result.InvalidIpv4
        return validatePortSuffix(portPart, Result.InvalidPort)
    }

    private fun validatePortSuffix(portPart: String, onInvalid: Result): Result {
        if (portPart.isEmpty()) return Result.Valid
        if (!portPart.startsWith(":")) return onInvalid
        val portStr = portPart.substring(1)
        if (portStr.isEmpty()) return onInvalid
        if (portStr.any { !it.isDigit() }) return onInvalid
        val port = portStr.toIntOrNull() ?: return onInvalid
        if (port !in 1..65535) return onInvalid
        return Result.Valid
    }

    private fun isValidIpv4(host: String): Boolean {
        val octets = host.split('.')
        if (octets.size != 4) return false
        for (octet in octets) {
            if (octet.isEmpty() || octet.length > 3) return false
            if (octet.any { !it.isDigit() }) return false
            // Reject leading zeros ("01", "001") to avoid octal-style ambiguity.
            if (octet.length > 1 && octet[0] == '0') return false
            val value = octet.toIntOrNull() ?: return false
            if (value !in 0..255) return false
        }
        return true
    }

    private fun isValidIpv6(host: String): Boolean {
        if (host.isEmpty()) return false
        // At most one "::" compression.
        val doubleColonCount = countOccurrences(host, "::")
        if (doubleColonCount > 1) return false

        val hasDoubleColon = doubleColonCount == 1
        val groups: List<String> = if (hasDoubleColon) {
            val (left, right) = host.split("::", limit = 2).let { it[0] to it[1] }
            val leftGroups = if (left.isEmpty()) emptyList() else left.split(':')
            val rightGroups = if (right.isEmpty()) emptyList() else right.split(':')
            // With compression total non-empty groups must be < 8.
            if (leftGroups.size + rightGroups.size >= 8) return false
            leftGroups + rightGroups
        } else {
            val parts = host.split(':')
            if (parts.size != 8) return false
            parts
        }

        for (group in groups) {
            if (group.isEmpty() || group.length > 4) return false
            if (group.any { !it.isHexDigit() }) return false
        }
        return true
    }

    private fun countOccurrences(haystack: String, needle: String): Int {
        var count = 0
        var idx = 0
        while (true) {
            val found = haystack.indexOf(needle, idx)
            if (found < 0) return count
            count++
            idx = found + needle.length
        }
    }

    private fun Char.isHexDigit(): Boolean =
        this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'
}
