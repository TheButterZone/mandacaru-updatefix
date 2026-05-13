package com.github.jvsena42.mandacaru.presentation.utils

import com.github.jvsena42.mandacaru.presentation.utils.PeerAddressValidator.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PeerAddressValidatorTest {

    // --- Valid IPv4 ---

    @Test
    fun `valid IPv4 with port`() {
        assertEquals(Result.Valid, PeerAddressValidator.validate("189.44.63.101:8333"))
    }

    @Test
    fun `valid IPv4 with non-default port`() {
        assertEquals(Result.Valid, PeerAddressValidator.validate("195.26.240.213:8433"))
    }

    @Test
    fun `valid IPv4 without port`() {
        assertEquals(Result.Valid, PeerAddressValidator.validate("161.97.178.61"))
    }

    @Test
    fun `valid IPv4 lower boundary`() {
        assertEquals(Result.Valid, PeerAddressValidator.validate("0.0.0.0"))
    }

    @Test
    fun `valid IPv4 upper boundary`() {
        assertEquals(Result.Valid, PeerAddressValidator.validate("255.255.255.255"))
    }

    @Test
    fun `valid IPv4 with min port 1`() {
        assertEquals(Result.Valid, PeerAddressValidator.validate("1.2.3.4:1"))
    }

    @Test
    fun `valid IPv4 with max port 65535`() {
        assertEquals(Result.Valid, PeerAddressValidator.validate("1.2.3.4:65535"))
    }

    @Test
    fun `valid IPv4 is trimmed of surrounding whitespace`() {
        assertEquals(Result.Valid, PeerAddressValidator.validate("  189.44.63.101:8333  "))
    }

    // --- Valid IPv6 ---

    @Test
    fun `valid bracketed IPv6 with port`() {
        assertEquals(Result.Valid, PeerAddressValidator.validate("[2001:db8::1]:8333"))
    }

    @Test
    fun `valid bracketed IPv6 full form with port`() {
        assertEquals(
            Result.Valid,
            PeerAddressValidator.validate("[2001:fb1:42:567:ea9c:25ff:fe79:744]:8333")
        )
    }

    @Test
    fun `valid bracketed IPv6 loopback without port`() {
        assertEquals(Result.Valid, PeerAddressValidator.validate("[::1]"))
    }

    @Test
    fun `valid bracketed IPv6 all-zero compressed`() {
        assertEquals(Result.Valid, PeerAddressValidator.validate("[::]"))
    }

    // --- Empty / whitespace ---

    @Test
    fun `empty string is Empty`() {
        assertEquals(Result.Empty, PeerAddressValidator.validate(""))
    }

    @Test
    fun `whitespace only is Empty`() {
        assertEquals(Result.Empty, PeerAddressValidator.validate("   "))
    }

    // --- Invalid IPv4 ---

    @Test
    fun `IPv4 octet above 255 is invalid`() {
        assertEquals(Result.InvalidIpv4, PeerAddressValidator.validate("256.0.0.1"))
    }

    @Test
    fun `IPv4 with too few octets is invalid`() {
        assertEquals(Result.InvalidIpv4, PeerAddressValidator.validate("1.2.3"))
    }

    @Test
    fun `IPv4 with too many octets is invalid`() {
        assertEquals(Result.InvalidIpv4, PeerAddressValidator.validate("1.2.3.4.5"))
    }

    @Test
    fun `IPv4 with non-numeric octet is invalid`() {
        assertEquals(Result.InvalidIpv4, PeerAddressValidator.validate("a.b.c.d"))
    }

    @Test
    fun `IPv4 with leading zero octet is invalid`() {
        assertEquals(Result.InvalidIpv4, PeerAddressValidator.validate("01.2.3.4"))
    }

    @Test
    fun `IPv4 with empty octet is invalid`() {
        assertEquals(Result.InvalidIpv4, PeerAddressValidator.validate("1..3.4"))
    }

    @Test
    fun `IPv4 with trailing dot is invalid`() {
        assertEquals(Result.InvalidIpv4, PeerAddressValidator.validate("1.2.3.4."))
    }

    // --- Invalid IPv6 / format ---

    @Test
    fun `bare unbracketed IPv6 is invalid format`() {
        // Multiple colons without brackets is not IPv4-with-port and not bracketed IPv6.
        assertEquals(Result.InvalidFormat, PeerAddressValidator.validate("2001:db8::1:8333"))
    }

    @Test
    fun `missing closing bracket is invalid IPv6`() {
        assertEquals(Result.InvalidIpv6, PeerAddressValidator.validate("[2001:db8::1"))
    }

    @Test
    fun `non-hex inside brackets is invalid IPv6`() {
        assertEquals(Result.InvalidIpv6, PeerAddressValidator.validate("[zzz]"))
    }

    @Test
    fun `empty brackets is invalid IPv6`() {
        assertEquals(Result.InvalidIpv6, PeerAddressValidator.validate("[]:8333"))
    }

    @Test
    fun `IPv6 with double compression is invalid`() {
        assertEquals(Result.InvalidIpv6, PeerAddressValidator.validate("[2001::1::2]:8333"))
    }

    @Test
    fun `IPv6 group longer than 4 chars is invalid`() {
        assertEquals(Result.InvalidIpv6, PeerAddressValidator.validate("[20011:db8::1]:8333"))
    }

    @Test
    fun `IPv6 uncompressed with wrong number of groups is invalid`() {
        assertEquals(Result.InvalidIpv6, PeerAddressValidator.validate("[2001:db8:1:2:3:4:5]"))
    }

    // --- Invalid port ---

    @Test
    fun `IPv4 with empty port is invalid port`() {
        assertEquals(Result.InvalidPort, PeerAddressValidator.validate("1.2.3.4:"))
    }

    @Test
    fun `IPv4 with port zero is invalid port`() {
        assertEquals(Result.InvalidPort, PeerAddressValidator.validate("1.2.3.4:0"))
    }

    @Test
    fun `IPv4 with port above max is invalid port`() {
        assertEquals(Result.InvalidPort, PeerAddressValidator.validate("1.2.3.4:65536"))
    }

    @Test
    fun `IPv4 with non-numeric port is invalid port`() {
        assertEquals(Result.InvalidPort, PeerAddressValidator.validate("1.2.3.4:abc"))
    }

    @Test
    fun `IPv4 with signed port is invalid port`() {
        assertEquals(Result.InvalidPort, PeerAddressValidator.validate("1.2.3.4:-1"))
    }

    @Test
    fun `IPv6 with empty port is invalid port`() {
        assertEquals(Result.InvalidPort, PeerAddressValidator.validate("[2001:db8::1]:"))
    }

    @Test
    fun `IPv6 with port above max is invalid port`() {
        assertEquals(Result.InvalidPort, PeerAddressValidator.validate("[2001:db8::1]:65536"))
    }

    // --- Sanity: ensure all Invalid* results are distinct types ---

    @Test
    fun `invalid results are distinct types`() {
        assertTrue(PeerAddressValidator.validate("256.0.0.1") is Result.InvalidIpv4)
        assertTrue(PeerAddressValidator.validate("[zzz]") is Result.InvalidIpv6)
        assertTrue(PeerAddressValidator.validate("1.2.3.4:0") is Result.InvalidPort)
        assertTrue(PeerAddressValidator.validate("2001:db8::1:8333") is Result.InvalidFormat)
    }
}
