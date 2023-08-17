/*
 * Copyright 2023 kotlin-bitcoin-tools and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.bitcointools.bip21

import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Represents a BIP-21 URI.
 *
 * See https://github.com/bitcoin/bips/blob/master/bip-0021.mediawiki for specification.
 */
public data class Bip21URI(
    public val address: String,
    public val amount: Satoshi? = null,
    public val label: String? = null,
    public val message: String? = null,
    public val lightning: String? = null,
    public val otherParameters: Map<String, String>? = null,
) {
    /**
     * Return a string representation of the URI.
     */
    public fun toURI(): String {
        StringBuilder("bitcoin:$address").let { builder ->
            if (amount == null && label == null && message == null && otherParameters == null) {
                return builder.toString()
            } else {
                builder.append("?")

                builder.append(amount?.let {
                    if (builder.last() == '?') "amount=${it.toBitcoin()}" else "&amount=${it.toBitcoin()}"
                } ?: "")

                builder.append(label?.let {
                    if (builder.last() == '?') "label=${encodeURLString(it)}" else "&label=${encodeURLString(it)}"
                } ?: "")

                builder.append(message?.let {
                    if (builder.last() == '?') "message=${encodeURLString(it)}" else "&message=${encodeURLString(it)}"
                } ?: "")

                builder.append(lightning?.let {
                    if (builder.last() == '?') "lightning=$it" else "&lightning=$it"
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
         * Parse a string into a Bip21URI.
         */
        public fun fromString(input: String): Bip21URI {
            val uri = URI.create(input)
            require(uri.scheme.lowercase() == "bitcoin") { "Invalid scheme: ${uri.scheme}" }

            // TODO: Because we don't validate addresses in this library, it's possible for the URI to be malformed by missing
            //       the ? character between the address and the parameters, and the Bip21URI will still be created with an invalid
            //       address. Example: parsing `bitcoin:1andreas3batLhQa2FawWjeyjCqyBzypdamount=50&label=Luke-Jr` would create
            //       bip21Uri.address = "1andreas3batLhQa2FawWjeyjCqyBzypdamount=50&label=Luke-Jr".

            // If the string contains a ? character, we deconstruct it into the address and other parameters parts
            // otherwise we return a Bip21URI with only the address
            val (address, parameters) = uri.schemeSpecificPart.find { it == '?' }?.let {
                uri.schemeSpecificPart.split("?", limit = 2)
            } ?: return Bip21URI(address = uri.schemeSpecificPart)

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

            val amount: Satoshi? = parametersMap["amount"]?.satoshi()
            val label: String? = parametersMap["label"]
            val message: String? = parametersMap["message"]
            val lightning: String? = parametersMap["lightning"]
            val other: Map<String, String>? = parametersMap
                .filterKeys { it !in setOf("amount", "label", "message", "lightning") }
                .takeIf { it.isNotEmpty() }

            return Bip21URI(
                address = address,
                amount = amount,
                label = label,
                message = message,
                lightning = lightning,
                otherParameters = other
            )
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
