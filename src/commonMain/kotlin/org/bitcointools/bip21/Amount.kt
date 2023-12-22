/*
 * Copyright 2023 kotlin-bitcoin-tools and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.bitcointools.bip21

import com.ionspin.kotlin.bignum.decimal.toBigDecimal

private const val MAX_SATOSHI: Long = 2_100_000_000_000_000
private const val MAX_BITCOIN: Long = 21_000_000

/**
 * Utility class to represent a satoshi amount.
 */
public data class Amount(public val sat: Long) : Parameter {
    init {
        require(sat <= MAX_SATOSHI) { "Satoshi value must be less than or equal to $MAX_SATOSHI" }
        require(sat >= 0) { "Satoshi value must be greater than or equal to 0" }
    }

    /**
     * Alternate constructor to build a [Amount] from a String.
     */
    public constructor(sat: String) : this(sat.toLong())

    /**
     * Convert a satoshi amount to a BIP-21 valid bitcoin amount of type String.
     */
    public fun toBitcoin(): String = sat.toBigDecimal().divide(100_000_000.toBigDecimal()).toStringExpanded()

    /**
     * Encode the amount to a string in the format that is required by the BIP-21 specification.
     */
    public override fun encode(): String = "&amount=${this.toBitcoin()}"

    public companion object {
        /**
         * Converts a bitcoin amount in string format to the [Amount] type.
         * The BIP-21 specification requires the amount be in bitcoin using the dot (.) as a decimal separator,
         * but working with the bitcoin unit is messy. Better to use the satoshi unit directly.
         */
        public fun decode(amount: String): Amount {
            val bitcoin = amount.toBigDecimal()
            require(bitcoin >= 0.toBigDecimal()) { "Invalid amount: $amount (cannot be negative)" }
            require(bitcoin <= MAX_BITCOIN.toBigDecimal()) { "Invalid amount: $amount (above possible number of bitcoin)" }

            val satoshis = (bitcoin * 100_000_000.toBigDecimal()).toStringExpanded()
            require(!satoshis.contains(".")) { "Invalid amount: $amount (too many decimal places)"}

            return Amount(sat = satoshis.toLong())
        }
    }
}

/** Build an [Amount] from a Long. */
public fun Long.satoshi(): Amount = Amount(this)

/** Build an [Amount] from a bitcoin amount in String format. */
public fun String.fromBitcoinIntoAmount(): Amount = Amount.decode(this)
