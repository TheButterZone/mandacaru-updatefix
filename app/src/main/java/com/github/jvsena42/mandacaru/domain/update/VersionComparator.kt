package com.github.jvsena42.mandacaru.domain.update

object VersionComparator {

    /**
     * Returns true if [remote] is a newer version than [current].
     *
     * Supports:
     * - "1.2.3"
     * - "v1.2.3"
     * - "V1.2.3"
     * - "1.2"
     * - "1.2.3.4"
     * - GitHub-style messy tags like "v1.2.3-beta1" (beta suffix ignored)
     */
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
        return version
            .trim()
            .removePrefix("v")
            .removePrefix("V")
            // drop anything after first non-version suffix like "-beta", "-rc"
            .split("-", "_")
            .first()
            .split(".")
            .map { segment ->
                segment.takeWhile { it.isDigit() }
                    .toIntOrNull() ?: 0
            }
    }
}
