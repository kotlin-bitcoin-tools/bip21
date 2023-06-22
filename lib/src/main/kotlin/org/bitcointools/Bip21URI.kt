/*
 * Copyright 2023 kotlin-bitcoin-tools and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */

package org.bitcointools

import fr.acinq.bitcoin.Satoshi
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Represents a BIP-21 URI.
 *
 * See https://github.com/bitcoin/bips/blob/master/bip-0021.mediawiki for specification.
 */
public data class Bip21URI(
    public val address: Address,
    public val amount: Satoshi? = null,
    public val label: String? = null,
    public val message: String? = null,
    public val otherParameters: Map<String, String>? = null,
) {
    /**
     * Returns a string representation of the URI.
     */
    public fun toURI(): String {
        StringBuilder("bitcoin:${address.value}").let { builder ->
            if (amount == null && label == null && message == null && otherParameters == null) {
                return builder.toString()
            } else {
                builder.append("?")

                builder.append(amount?.let {
                    if (builder.last() == '?') "amount=${satoshiToBitcoin(it)}" else "&amount=${satoshiToBitcoin(it)}"
                } ?: "")

                builder.append(label?.let {
                    if (builder.last() == '?') "label=${encodeURLString(it)}" else "&label=${encodeURLString(it)}"
                } ?: "")

                builder.append(message?.let {
                    if (builder.last() == '?') "message=${encodeURLString(it)}" else "&message=${encodeURLString(it)}"
                } ?: "")

                otherParameters?.forEach { (key, value) ->
                    val element = if (builder.last() == '?') {
                        "${encodeURLString(key)}=${encodeURLString(value)}"
                    } else {
                        "&${encodeURLString(key)}=${encodeURLString(value)}"
                    }
                    builder.append(element)
                }

                return builder.toString()
            }
        }
    }

    public companion object {
        /**
         * Parses a string into a Bip21URI.
         *
         * The method requires a [Network] to be provided to validate the address.
         */
        public fun fromString(input: String, network: Network): Bip21URI {
            val uri = URI.create(input)
            require(uri.scheme.lowercase() == "bitcoin") { "Invalid scheme: ${uri.scheme}" }

            // If the string contains a ? character, we deconstruct it into the address and other parameters parts
            // otherwise we return a Bip21URI with only the address
            val (address, parameters) = uri.schemeSpecificPart.find { it == '?' }?.let {
                uri.schemeSpecificPart.split("?", limit = 2)
            } ?: return Bip21URI(address = Address(uri.schemeSpecificPart, network))

            require(parameters.isNotEmpty()) { "Invalid URI: parameters part is empty" }

            // Check for duplicate parameters and throw if any are found because the later ones would
            // override the earlier ones
            val keys: List<String> = parameters.split("&").map { it.split("=", limit = 2)[0] }
            if (keys.size != keys.toSet().size) {
                throw InvalidURIException("Invalid URI: duplicate parameter")
            }

            // Deconstruct the parameters part into a map of key-value pairs
            val parametersMap = parameters.split("&").associate { part ->
                val keyValueElement = part.split("=", limit = 2)
                require(keyValueElement.size == 2) { "Invalid URI: parameter $part does not have a separator" }

                val (key, value) = keyValueElement
                require(key.isNotEmpty()) { "Invalid URI: parameter $part has an empty key" }
                require(value.isNotEmpty()) { "Invalid URI: parameter $part has an empty value" }

                key to value
            }

            val validatedAddress = Address(address, network)
            val amount: Satoshi? = parametersMap["amount"]?.let { bitcoinToSatoshi(it) }
            val label: String? = parametersMap["label"]
            val message: String? = parametersMap["message"]
            val other: Map<String, String>? = parametersMap
                .filterKeys { it !in setOf("amount", "label", "message") }
                .takeIf { it.isNotEmpty() }

            return Bip21URI(
                address = validatedAddress,
                amount = amount,
                label = label,
                message = message,
                otherParameters = other
            )
        }

        /**
         * Converts a bitcoin amount in string format to the Satoshi type defined in fr.acinq.bitcoin.Satoshi.
         * The BIP-21 specification requires the amount to be in bitcoin using the dot (.) as a decimal separator,
         * but working with the bitcoin unit is messy. Better to use the Satoshi type directly.
         */
        private fun bitcoinToSatoshi(amount: String): Satoshi {
            val bitcoin = amount.toBigDecimal()
            require(bitcoin >= 0.toBigDecimal()) { "Invalid amount: $amount (cannot be negative)" }
            require(bitcoin <= 21_000_000.toBigDecimal()) { "Invalid amount: $amount (above possible number of bitcoin)" }

            try {
                val satoshi = bitcoin.multiply(100_000_000.toBigDecimal()).longValueExact()
                return Satoshi(satoshi)
            } catch (e: ArithmeticException) {
                throw InvalidURIException("Invalid amount: $amount (too many decimal places)")
            }
        }

        /**
         * Converts a Satoshi amount to a BIP-21 valid bitcoin amount in string format.
         * The BIP-21 specification requires the amount to be in bitcoin using the dot (.) as a decimal separator.
         */
        private fun satoshiToBitcoin(amount: Satoshi): String {
            return amount.sat.toBigDecimal().divide(100_000_000.toBigDecimal()).toString()
        }

        /**
         * Encodes a string to be used in the URI. This is required for the label and message parameters which might
         * contain spaces and other special characters. Notice that the URLEncoder class replaces spaces with the
         * plus (+) character but the BIP-21 specification requires the percent-encoded space (%20) to be used
         * as per RFC 3986.
         */
        private fun encodeURLString(stringToEncode: String): String {
            return URLEncoder.encode(stringToEncode, StandardCharsets.UTF_8).replace("+", "%20")
        }
    }
}
