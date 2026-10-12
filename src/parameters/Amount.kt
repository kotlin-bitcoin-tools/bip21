/*
 * Copyright 2023-2025 Kotlin Bitcoin Tools contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.kotlinbitcointools.bip21.parameters

private const val MAX_SATOSHI: Long = 2_100_000_000_000_000
private const val SATOSHIS_PER_BITCOIN: Long = 100_000_000

/**
 * Check that a string follows the BIP-21 grammar for amounts, `*digit [ "." *digit ]`: digits with an optional '.'
 * decimal separator. No sign, exponent, or thousands separator is allowed. We also require at least one digit, so ""
 * and "." are rejected.
 */
private fun isBip21AmountFormat(amount: String): Boolean {
    val parts: List<String> = amount.split(".")
    val hasAtMostTwoParts = parts.size <= 2
    val hasAtLeastOneDigit = parts.any { it.isNotEmpty() }
    val hasOnlyAsciiDigits = parts.all { part -> part.all { it in '0'..'9' } }

    return hasAtMostTwoParts && hasAtLeastOneDigit && hasOnlyAsciiDigits
}

/** Utility class to represent a satoshi amount. */
public data class Amount(
    public val sat: Long,
) : Parameter {
    init {
        require(sat <= MAX_SATOSHI) { "Satoshi value must be less than or equal to $MAX_SATOSHI" }
        require(sat >= 0) { "Satoshi value must be greater than or equal to 0" }
    }

    /** Alternate constructor to build a [Amount] from a String. */
    public constructor(sat: String) : this(sat.toLong())

    /** Convert a satoshi amount to a BIP-21 valid bitcoin amount of type String. */
    public fun toBitcoin(): String = sat.toBigDecimal().divide(100_000_000.toBigDecimal()).toPlainString()

    /** Encode the amount to a string in the format that is required by the BIP-21 specification. */
    public override fun encode(): String = "&amount=${this.toBitcoin()}"

    public companion object {
        /**
         * Converts a bitcoin amount in string format to the [Amount] type. The BIP-21 specification requires the amount
         * be in bitcoin using the dot (.) as a decimal separator, but working with the bitcoin unit is messy. Better to
         * use the satoshi unit directly.
         */
        public fun decode(uriString: String): Amount {
            require(isBip21AmountFormat(uriString)) {
                "Invalid amount: $uriString (must contain only digits and at most one '.' decimal separator)"
            }

            // Split an amount like "12.345" into its whole bitcoin part "12" and its fractional part "345". Either part
            // can be empty (".5" or "5."). Leading zeros of the whole part don't change the value, so we drop them.
            val wholePart: String = uriString.substringBefore(".").trimStart('0')
            val fractionalPart: String = uriString.substringAfter(".", missingDelimiterValue = "")

            // The maximum of 21,000,000 bitcoin has 8 digits, so a whole part with more digits is always too large.
            // Checking this first also guarantees that converting the whole part to a Long below cannot overflow.
            require(wholePart.length <= 8) { "Invalid amount: $uriString (above possible number of bitcoin)" }

            // One satoshi is 0.00000001 bitcoin, so any digit after the 8th decimal place is a fraction of a satoshi.
            // We also reject trailing zeros beyond the 8th decimal place: they don't change the value, but an amount
            // like 0.000000010 (1 satoshi) is easy to misread as 10 satoshis, and hints at a payee misunderstanding
            // the satoshi precision of bitcoin amounts.
            require(fractionalPart.length <= 8) { "Invalid amount: $uriString (too many decimal places)" }

            // The fractional part is padded with zeros to exactly 8 digits, which is its value in satoshis.
            val satoshis: Long =
                wholePart.ifEmpty { "0" }.toLong() * SATOSHIS_PER_BITCOIN + fractionalPart.padEnd(8, '0').toLong()
            require(satoshis <= MAX_SATOSHI) { "Invalid amount: $uriString (above possible number of bitcoin)" }
            require(satoshis > 0) { "Invalid amount: $uriString (amount is below 1 satoshi)" }

            return Amount(sat = satoshis)
        }
    }
}

/** Build an [Amount] from a Long. */
public fun Long.satoshi(): Amount = Amount(this)

/** Build an [Amount] from a bitcoin amount in String format. */
public fun String.fromBitcoinIntoAmount(): Amount = Amount.decode(this)
