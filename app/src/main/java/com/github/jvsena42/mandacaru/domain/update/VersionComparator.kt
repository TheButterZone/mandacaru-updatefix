package com.github.jvsena42.mandacaru.domain.update

object VersionComparator {

    fun isNewer(remote: String, current: String): Boolean {
        val remoteParts = parse(remote)
        val currentParts = parse(current)
        val size = maxOf(remoteParts.size, currentParts.size)
        for (index in 0 until size) {
            val remoteValue = remoteParts.getOrElse(index) { 0 }
            val currentValue = currentParts.getOrElse(index) { 0 }
            if (remoteValue != currentValue) return remoteValue > currentValue
        }
        return false
    }

    private fun parse(version: String): List<Int> =
        version.trim()
            .removePrefix("v")
            .removePrefix("V")
            .split(".")
            .map { part -> part.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
}
