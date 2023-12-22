/*
 * Copyright 2023 kotlin-bitcoin-tools and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.bitcointools.bip21

import com.eygraber.uri.Uri
import org.bitcointools.bip21.parameters.Amount
import org.bitcointools.bip21.parameters.Label
import org.bitcointools.bip21.parameters.Lightning
import org.bitcointools.bip21.parameters.Message
import org.bitcointools.bip21.parameters.OtherParameter
import org.bitcointools.bip21.parameters.fromBitcoinIntoAmount

/**
 * Represents a BIP-21 URI.
 *
 * See [https://github.com/bitcoin/bips/blob/master/bip-0021.mediawiki](https://github.com/bitcoin/bips/blob/master/bip-0021.mediawiki)
 * for specification.
 *
 * Note that while it is common for raw addresses to be displayed in QR format, they are not valid BIP21 URIs on their
 * own and will fail the check on the scheme when attempting to parse them using Bip21URI.fromString(). Users should
 * parse the string returned by the scanner or other source and validate that the `bitcoin:` scheme is present before
 * attempting to build a Bip21URI.
 */
public data class Bip21URI(
    public val address: String,
    public val amount: Amount? = null,
    public val label: Label? = null,
    public val message: Message? = null,
    public val lightning: Lightning? = null,
    public val otherParameters: List<OtherParameter>? = null,
) {
    /**
     * Return a string representation of the URI.
     */
    public fun toURI(): String {
        val schemeAndAddress: String = "bitcoin:$address"

        if (amount == null && label == null && message == null && otherParameters == null) {
            return schemeAndAddress
        }

        val parameters = StringBuilder().let { builder ->
            builder.append(amount?.let { amount.encode() } ?: "")
            builder.append(label?.let { label.encode() } ?: "")
            builder.append(message?.let { message.encode() } ?: "")
            builder.append(lightning?.let { lightning.encode() } ?: "")
            otherParameters?.forEach { otherParameter -> builder.append(otherParameter.encode()) }
            builder.deleteAt(0) // Remove the first '&' character
            builder.toString()
        }

        return "$schemeAndAddress?$parameters"
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
            val parametersMap: Map<String, String> = buildParametersMap(query)

            val amount: Amount? = parametersMap["amount"]?.fromBitcoinIntoAmount()
            val label: Label? = parametersMap["label"]?.let { Label(it) }
            val message: Message? = parametersMap["message"]?.let { Message(it) }
            val lightning: Lightning? = parametersMap["lightning"]?.let { Lightning(it) }
            val other: List<OtherParameter>? = parametersMap
                .filterKeys { it !in setOf("amount", "label", "message", "lightning") }
                .takeIf { it.isNotEmpty() }
                ?.map { OtherParameter(it.key, it.value) }

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
         * Deconstruct the query part into a map of key-value pairs. This method also checks that there are no duplicate
         * keys and throw if any are found because the later ones would override the earlier ones.
         */
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
