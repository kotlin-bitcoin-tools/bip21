/*
 * Copyright 2023 kotlin-bitcoin-tools and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.bitcointools.bip21

import com.eygraber.uri.Uri
import io.ktor.http.encodeURLQueryComponent

/**
 * Represents a BIP-21 URI.
 *
 * See [https://github.com/bitcoin/bips/blob/master/bip-0021.mediawiki](https://github.com/bitcoin/bips/blob/master/bip-0021.mediawiki) for specification.
 *
 * Note that while it is common for raw addresses to be displayed in QR format, they are not valid BIP21 URIs and will
 * fail the check on the scheme when attempting to parse them using Bip21URI.fromString(). Users should parse the string
 * returned by the scanner or other source and validate that the `bitcoin:` scheme is present before attempting to build
 * a Bip21URI.
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
        // The BIP-21 specification requires strings for query components be sanitized to make
        // them safe to include in URIs. Moreover, the percent-encoded space (%20) must be used as per RFC 3986
        // instead of the sometimes-used '+' character. This string encoding is what the
        // io.ktor.http.encodeURLQueryComponent() String extension function provides.
        StringBuilder("bitcoin:$address").let { builder ->
            if (amount == null && label == null && message == null && otherParameters == null) {
                return builder.toString()
            } else {
                builder.append("?")

                builder.append(amount?.let {
                    if (builder.last() == '?') "amount=${it.toBitcoin()}" else "&amount=${it.toBitcoin()}"
                } ?: "")

                builder.append(label?.let {
                    if (builder.last() == '?') "label=${it.encodeURLQueryComponent(encodeFull = true)}" else "&label=${it.encodeURLQueryComponent(encodeFull = true)}"
                } ?: "")

                builder.append(message?.let {
                    if (builder.last() == '?') "message=${it.encodeURLQueryComponent(encodeFull = true)}" else "&message=${it.encodeURLQueryComponent(encodeFull = true)}"
                } ?: "")

                builder.append(lightning?.let {
                    if (builder.last() == '?') "lightning=${it.encodeURLQueryComponent()}" else "&lightning=${it.encodeURLQueryComponent()}"
                } ?: "")

                otherParameters?.forEach { (key, value) ->
                    val element = if (builder.last() == '?') {
                        "${key.encodeURLQueryComponent(encodeFull = true)}=${value.encodeURLQueryComponent(encodeFull = true)}"
                    } else {
                        "&${key.encodeURLQueryComponent(encodeFull = true)}=${value.encodeURLQueryComponent(encodeFull = true)}"
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
         *
         * Note that because we don't validate addresses in this library, it's possible to have a correctly formed BIP-0021
         * URI that does not have a valid address. Users should validate themselves the address for the network they use.
         */
        public fun fromString(input: String): Bip21URI {
            val uri = Uri.parse(input)
            require(uri.scheme?.lowercase() == "bitcoin") {
                "Invalid scheme '${input.take(8)}', expected 'bitcoin:'"
            }

            // This recasts the nullable String? as String and allows us to not have to null-check
            // on further calls below. It's not clear when the schemeSpecificPart could be null (I've only ever seen it empty).
            val schemeSpecificPart: String = uri.schemeSpecificPart ?: throw InvalidURIException("Scheme specific part is null")
            require(schemeSpecificPart.isNotEmpty()) { "Invalid URI: missing bitcoin address" }

            // If the string contains a ? character, we deconstruct it into the address and other parameters parts.
            // If it doesn't contain a ?, we return a Bip21URI with only the address.
            val address = schemeSpecificPart.find { it == '?' }?.let {
                schemeSpecificPart.split("?", limit = 2)
            }?.first() ?: return Bip21URI(address = schemeSpecificPart)

            // We cannot use the uri.getQueryParameterNames() API to get the parameters and their values
            // because the URI is classified as non-hierarchical, which means the API is not available.
            val query = uri.query ?: throw InvalidURIException("Query part is null")
            val parametersMap = buildParametersMap(query)

            val amount: Satoshi? = parametersMap["amount"]?.fromBitcoinAmountToSatoshi()
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

        // Deconstruct the query part into a map of key-value pairs. This method also checks
        // that there are no duplicate keys and throw if any are found because the
        // later ones would override the earlier ones.
        private fun buildParametersMap(query: String): Map<String, String> {
            // If there are no characters past the ? character, throw
            require(query.isNotEmpty()) { "? character indicates query part but the part is empty" }

            val keys: List<String> = query.split("&").map { it.split("=", limit = 2)[0] }
            if (keys.size != keys.toSet().size) throw InvalidURIException("Invalid URI: duplicate parameter")

            return query.split("&").associate { part ->
                val keyValueElement = part.split("=", limit = 2)
                require(keyValueElement.size == 2) { "Invalid URI: parameter $part does not have a separator" }

                val (key, value) = keyValueElement
                require(key.isNotEmpty()) { "Invalid URI: parameter $part has an empty key" }
                require(value.isNotEmpty()) { "Invalid URI: parameter $part has an empty value" }

                key to value
            }
        }
    }
}
