package org.bitcointools.bip21

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
            val uri = Bip21URI.fromString("bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd")

            assertEquals(
                expected = "1andreas3batLhQa2FawWjeyjCqyBzypd",
                actual = uri.address
            )
            assertEquals(null, uri.amount)
            assertEquals(null, uri.label)
            assertEquals(null, uri.message)
            assertEquals(null, uri.otherParameters)
        }

        @Test
        fun `Schema is case insensitive`() {
            Bip21URI.fromString("BiTcOiN:1andreas3batLhQa2FawWjeyjCqyBzypd")
            Bip21URI.fromString("BITCOIN:1andreas3batLhQa2FawWjeyjCqyBzypd")
            Bip21URI.fromString("Bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd")
        }

        @Test
        fun `URI has bech32 address in all caps`() {
            val uri = Bip21URI.fromString("bitcoin:BC1Q0XCQPZRKY6EFF2G52QDYE53XKK9JXKVRH6YHYW")

            assertEquals(
                expected = "BC1Q0XCQPZRKY6EFF2G52QDYE53XKK9JXKVRH6YHYW",
                actual = uri.address
            )
        }

        @Test
        fun `URI with full standard parameters`() {
            val uri = Bip21URI.fromString("bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd?amount=50&label=Luke-Jr&message=Donation%20for%20project%20xyz")

            assertEquals(
                expected = "1andreas3batLhQa2FawWjeyjCqyBzypd",
                actual = uri.address
            )

            assertNotNull(uri.amount)
            assertEquals(Satoshi(5000000000), uri.amount)
            assertEquals("Luke-Jr", uri.label)
            assertEquals("Donation for project xyz", uri.message)
            assertEquals(null, uri.otherParameters)
        }

        @Test
        fun `URI has parameters we don't know`() {
            val uri = Bip21URI.fromString("bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd?amount=100&arg1=50&arg2=999&arg3=abc%20abc")

            assertEquals(
                expected = "1andreas3batLhQa2FawWjeyjCqyBzypd",
                actual = uri.address
            )

            assertNotNull(uri.amount)
            assertEquals(Satoshi(10_000_000_000L), uri.amount)
            assertEquals(null, uri.label)
            assertEquals(null, uri.message)
            assertEquals("50", uri.otherParameters?.get("arg1"))
            assertEquals("999", uri.otherParameters?.get("arg2"))
            assertEquals("abc abc", uri.otherParameters?.get("arg3"))
        }

        @Test
        fun `URI has extreme bitcoin amounts`() {
            Bip21URI.fromString("bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd?amount=21000000")
            Bip21URI.fromString("bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd?amount=21000000.0000000000000")
            Bip21URI.fromString("bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd?amount=0.00000001")
            Bip21URI.fromString("bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd?amount=000000000000.00000001")
        }

        @Test
        fun `Build URI using spaces in values of label, message, and other parameters`() {
            val uri = Bip21URI(
                address = "1andreas3batLhQa2FawWjeyjCqyBzypd",
                amount = Satoshi(5000000000),
                label = "Kotlin Bitcoin Tools",
                message = "Building tools for bitcoin in Kotlin",
                otherParameters = mapOf("otherparameter1" to "abc abc", "otherparameter2" to "def def")
            )
            assertEquals(
                expected = "bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd?amount=50&label=Kotlin%20Bitcoin%20Tools&message=Building%20tools%20for%20bitcoin%20in%20Kotlin&otherparameter1=abc%20abc&otherparameter2=def%20def",
                actual = uri.toURI()
            )
        }

        @Test
        fun `Build URI using spaces in names of parameters`() {
            val uri = Bip21URI(
                address = "1andreas3batLhQa2FawWjeyjCqyBzypd",
                otherParameters = mapOf("other parameter 1" to "abc abc", "other parameter 2" to "def def")
            )
            assertEquals(
                expected = "bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd?other%20parameter%201=abc%20abc&other%20parameter%202=def%20def",
                actual = uri.toURI()
            )
        }
    }

    @Nested
    inner class Failures {
        @Test
        fun `URI doesn't use bitcoin scheme`() {
            val exception = assertFailsWith<IllegalArgumentException> {
                Bip21URI.fromString("https://example.com")
            }
            assertEquals("Invalid scheme: https", exception.message)
        }

        @Test
        fun `URI has no address`() {
            val exception = assertFailsWith<IllegalArgumentException> {
                Bip21URI.fromString("bitcoin:")
            }
            assertEquals("Expected scheme-specific part at index 8: bitcoin:", exception.message)
        }

        @Test
        fun `URI has question mark character but empty query`() {
            val exception = assertFailsWith<IllegalArgumentException> {
                Bip21URI.fromString("bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd?")
            }
            assertEquals("Invalid URI: parameters part is empty", exception.message)
        }

        @Test
        fun `URI has a parameter missing an = sign`() {
            val exception = assertFailsWith<IllegalArgumentException> {
                Bip21URI.fromString("bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd?amount100&arg1=50&arg2=999&arg3=abc%20abc")
            }
            assertEquals("Invalid URI: parameter amount100 does not have a separator", exception.message)
        }

        @Test
        fun `URI has invalid bitcoin amounts`() {
            val exception1 = assertFailsWith<IllegalArgumentException> {
                Bip21URI.fromString("bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd?amount=21000001")
            }
            val exception2 = assertFailsWith<IllegalArgumentException> {
                Bip21URI.fromString("bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd?amount=21000000.00000001")
            }
            val exception3 = assertFailsWith<IllegalArgumentException> {
                Bip21URI.fromString("bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypd?amount=0.000000001")
            }

            assertEquals("Invalid amount: 21000001 (above possible number of bitcoin)", exception1.message)
            assertEquals("Invalid amount: 21000000.00000001 (above possible number of bitcoin)", exception2.message)
            assertEquals("Invalid amount: 0.000000001 (too many decimal places)", exception3.message)
        }
    }
}