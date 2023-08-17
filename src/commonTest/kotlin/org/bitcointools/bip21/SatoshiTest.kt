package org.bitcointools.bip21

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SatoshiTest {
    @Test
    fun `Satoshi can be created`() {
        val amount = Satoshi(1000L)
        assertEquals(1000L, amount.sat)
    }

    @Test
    fun `Satoshi can be created from bitcoin amount formatted string`() {
        val amount0 = "0.001".fromBitcoinAmountToSatoshi()
        assertEquals(100_000L, amount0.sat)

        val amount1 = "212121.00000001".fromBitcoinAmountToSatoshi()
        assertEquals(21212100000001L, amount1.sat)

        val amount2 = "100".fromBitcoinAmountToSatoshi()
        assertEquals(10_000_000_000L, amount2.sat)
    }

    @Test
    fun `Satoshi amount cannot be negative`() {
        assertFailsWith<IllegalArgumentException> {
            Satoshi(-1000L)
        }
    }

    @Test
    fun `Bitcoin amount cannot be negative`() {
        assertFailsWith<IllegalArgumentException> {
            "-0.001".fromBitcoinAmountToSatoshi()
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
            tooManyDecimalsAmount.fromBitcoinAmountToSatoshi()
        }
        assertFailsWith<IllegalArgumentException> {
            tooManyBitcoinAmount.fromBitcoinAmountToSatoshi()
        }
    }
}
