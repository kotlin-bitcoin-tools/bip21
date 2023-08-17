package org.bitcointools.bip21

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SatoshiTest {
    @Nested
    inner class Successes {
        @Test
        fun `Satoshi can be created`() {
            val amount = Satoshi(1000L)
            assertEquals(1000L, amount.sat)
        }

        @Test
        fun `Satoshi can be created from bitcoin amount formatted string()`() {
            val amount0 = "0.001".satoshi()
            assertEquals(100_000L, amount0.sat)

            val amount1 = "212121.00000001".satoshi()
            assertEquals(21212100000001L, amount1.sat)

            val amount2 = "100".satoshi()
            assertEquals(10_000_000_000L, amount2.sat)
        }
    }

    @Nested
    inner class Failures {
        @Test
        fun `Satoshi amount cannot be negative`() {
            assertFailsWith<IllegalArgumentException> {
                Satoshi(-1000L)
            }
        }

        @Test
        fun `Bitcoin amount cannot be negative`() {
            assertFailsWith<IllegalArgumentException> {
                "-0.001".satoshi()
            }
        }

        @Test
        fun `Satoshi amount cannot be above MAX_SATOSHI value`() {
            assertFailsWith<IllegalArgumentException> {
                Satoshi(Long.MAX_VALUE)
            }
        }

        @Test
        fun `Error thrown when building with invalid bitcoin amount`() {
            val tooManyDecimalsAmount = "0.000000001"
            val tooManyBitcoinAmount = "21000001"

            assertFailsWith<IllegalArgumentException> {
                tooManyDecimalsAmount.satoshi()
            }
            assertFailsWith<IllegalArgumentException> {
                tooManyBitcoinAmount.satoshi()
            }
        }
    }
}
