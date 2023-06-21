package org.bitcointools

import fr.acinq.bitcoin.Satoshi
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class Bip21URITest {
    @Nested
    inner class Successes {
        @Test
        fun `URI only has bitcoin address`() {
            val uri = Bip21URI.fromString("bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd", Network.MAINNET)

            assertEquals<String>(
                expected = Address("1andreas3batLhQa2FawWjeyjCqyBzypd", Network.MAINNET).value,
                actual = uri.address.value
            )
            assertEquals(null, uri.amount)
            assertEquals(null, uri.label)
            assertEquals(null, uri.message)
            assertEquals(null, uri.otherParameters)
        }

        @Test
        fun `URI with full standard parameters`() {
            val uri = Bip21URI.fromString("bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd?amount=50&label=Luke-Jr&message=Donation%20for%20project%20xyz", Network.MAINNET)

            assertEquals(
                expected = Address("1andreas3batLhQa2FawWjeyjCqyBzypd", Network.MAINNET).value,
                actual = uri.address.value
            )

            assertNotNull(uri.amount)
            assertEquals(Satoshi(5000000000), uri.amount)
            assertEquals("Luke-Jr", uri.label)
            assertEquals("Donation for project xyz", uri.message)
            assertEquals(null, uri.otherParameters)
        }

        @Test
        fun `URI has parameters we don't know`() {
            val uri = Bip21URI.fromString("bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd?amount=100&arg1=50&arg2=999&arg3=abc%20abc", Network.MAINNET)

            assertEquals(
                expected = Address("1andreas3batLhQa2FawWjeyjCqyBzypd", Network.MAINNET).value,
                actual = uri.address.value
            )

            assertNotNull(uri.amount)
            assertEquals(Satoshi(10_000_000_000L), uri.amount)
            assertEquals(null, uri.label)
            assertEquals(null, uri.message)
            assertEquals("50", uri.otherParameters?.get("arg1"))
            assertEquals("999", uri.otherParameters?.get("arg2"))
            assertEquals("abc abc", uri.otherParameters?.get("arg3"))
        }
    }

    @Nested
    inner class Failures {
        @Test
        fun `URI doesn't use bitcoin scheme`() {
            val exception = assertFailsWith<IllegalArgumentException> {
                Bip21URI.fromString("https://example.com", Network.MAINNET)
            }
            assertEquals("Invalid scheme: https", exception.message)
        }

        @Test
        fun `URI has no address`() {
            val exception = assertFailsWith<IllegalArgumentException> {
                Bip21URI.fromString("bitcoin:", Network.MAINNET)
            }
            assertEquals("Expected scheme-specific part at index 8: bitcoin:", exception.message)
        }

        @Test
        fun `URI has invalid address`() {
            assertFailsWith<IllegalStateException> {
                Bip21URI.fromString("bitcoin:1yjCqd", Network.MAINNET)
            }
        }

        @Test
        fun `URI has valid address but wrong network`() {
            val exception = assertFailsWith<IllegalStateException> {
                Bip21URI.fromString("bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd", Network.TESTNET)
            }
            assertEquals("base58 address does not match our blockchain", exception.message)
        }

        @Test
        fun `URI has question mark character but empty query`() {
            val exception = assertFailsWith<IllegalArgumentException> {
                Bip21URI.fromString("bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd?", Network.TESTNET)
            }
            assertEquals("Invalid URI: parameters part is empty", exception.message)
        }

        @Test
        fun `URI is missing question mark between path and query`() {
            assertFailsWith<IllegalStateException> {
                Bip21URI.fromString("bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypdamount=50&label=Luke-Jr&message=Donation%20for%20project%20xyz", Network.MAINNET)
            }
        }

        @Test
        fun `URI has a parameter missing an = sign`() {
            val exception = assertFailsWith<IllegalArgumentException> {
                Bip21URI.fromString("bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd?amount100&arg1=50&arg2=999&arg3=abc%20abc", Network.MAINNET)
            }
            assertEquals("Invalid URI: parameter amount100 does not have a separator", exception.message)
        }
    }
}