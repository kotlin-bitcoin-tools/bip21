package org.bitcointools.bip21

import java.math.BigDecimal

private const val MAX_SATOSHI: Long = 2_100_000_000_000_000

/**
 * Utility class to represent a satoshi amount.
 */
@JvmInline
public value class Satoshi(public val sat: Long) {
    init {
        require(sat <= MAX_SATOSHI) { "Satoshi value must be less than or equal to $MAX_SATOSHI" }
        require(sat >= 0) { "Satoshi value must be greater than or equal to 0" }
    }

    /**
     * Converts a satoshi amount to a BIP-21 valid bitcoin amount.
     */
    public fun toBitcoin(): BigDecimal = sat.toBigDecimal().divide(BigDecimal(100_000_000))

    public companion object {
        /**
         * Converts a bitcoin amount in string format to the Satoshi type.
         * The BIP-21 specification requires the amount be in bitcoin using the dot (.) as a decimal separator,
         * but working with the bitcoin unit is messy. Better to use the Satoshi type directly.
         */
        public fun fromBitcoin(amount: String): Satoshi {
            val bitcoin = amount.toBigDecimal()
            require(bitcoin >= 0.toBigDecimal()) { "Invalid amount: $amount (cannot be negative)" }
            require(bitcoin <= 21_000_000.toBigDecimal()) { "Invalid amount: $amount (above possible number of bitcoin)" }

            try {
                val satoshis = bitcoin.multiply(100_000_000.toBigDecimal()).longValueExact()
                return Satoshi(sat = satoshis)
            } catch (e: ArithmeticException) {
                throw IllegalArgumentException("Invalid amount: $amount (too many decimal places)")
            }
        }
    }
}

public fun Long.satoshi(): Satoshi = Satoshi(this)
public fun String.satoshi(): Satoshi = Satoshi.fromBitcoin(this)
