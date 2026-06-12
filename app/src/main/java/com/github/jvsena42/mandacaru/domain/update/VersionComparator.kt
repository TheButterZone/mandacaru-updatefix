package com.github.jvsena42.mandacaru.domain.update

object VersionComparator {

    fun isNewer(remote: String, current: String): Boolean {
        val remoteParts = parse(remote)
        val currentParts = parse(current)

        val size = maxOf(remoteParts.size, currentParts.size)

        for (i in 0 until size) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }

            if (r != c) return r > c
        }

        return false
    }

    private fun parse(version: String): List<Int> {
        val clean = version.trim()
            .removePrefix("v")
            .removePrefix("V")

        if (clean.isEmpty()) return listOf(0)

        return clean.split(".")
            .map { part ->
                part.takeWhile(Char::isDigit)
                    .toIntOrNull() ?: 0
            }
    }
}
