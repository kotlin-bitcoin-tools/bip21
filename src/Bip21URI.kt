/*
 * Copyright 2023-2025 Kotlin Bitcoin Tools contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.kotlinbitcointools.bip21

import io.ktor.http.decodeURLQueryComponent
import java.net.URI
import org.kotlinbitcointools.bip21.parameters.Amount
import org.kotlinbitcointools.bip21.parameters.Label
import org.kotlinbitcointools.bip21.parameters.Lightning
import org.kotlinbitcointools.bip21.parameters.Message
import org.kotlinbitcointools.bip21.parameters.OtherParameter
import org.kotlinbitcointools.bip21.parameters.PayJoin
import org.kotlinbitcointools.bip21.parameters.fromBitcoinIntoAmount

/**
 * Represents a BIP-21 URI.
 *
 * See
 * [https://github.com/bitcoin/bips/blob/master/bip-0021.mediawiki](https://github.com/bitcoin/bips/blob/master/bip-0021.mediawiki)
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
    public val pj: PayJoin? = null,
    public val otherParameters: List<OtherParameter>? = null,
) {
    init {
        // This library doesn't validate addresses for a specific network, but all bitcoin addresses (base58 and
        // bech32, in lowercase or uppercase) only use ASCII letters and digits. Rejecting anything else prevents
        // characters like '?', '&', or '#' in the address from changing the meaning of the encoded URI.
        require(address.isNotEmpty()) { "Invalid address: address cannot be empty" }
        require(address.all { it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' }) {
            "Invalid address '$address': addresses can only contain ASCII letters and digits"
        }
    }

    // The pjos parameter is required under BIP-078 (PayJoin V1) but should always be set to 0 (false) as per BIP-077
    // (PayJoin V2). This library supports PayJoin V2 and sets the pjos parameter to false anytime there is a pj
    // parameter defined.
    public val pjos: Boolean? = if (pj != null) false else null

    /** Return a string representation of the URI. */
    public fun toURI(): String {
        val schemeAndAddress: String = "bitcoin:$address"

        val parameters =
            StringBuilder().let { builder ->
                builder.append(amount?.let { amount.encode() } ?: "")
                builder.append(label?.let { label.encode() } ?: "")
                builder.append(message?.let { message.encode() } ?: "")
                builder.append(lightning?.let { lightning.encode() } ?: "")
                builder.append(pj?.let { pj.encode() } ?: "")
                if (pj != null) builder.append("&pjos=0")
                otherParameters?.forEach { otherParameter -> builder.append(otherParameter.encode()) }
                builder.toString().removePrefix("&") // Remove the first '&' character
            }

        // If no parameters were added, the URI is just the scheme and address, without a '?'.
        return if (parameters.isEmpty()) schemeAndAddress else "$schemeAndAddress?$parameters"
    }

    override fun toString(): String {
        return "Bip21URI(address=$address, amount=$amount, label=$label, message=$message, lightning=$lightning, pj=$pj, pjos=$pjos, otherParameters=$otherParameters)"
    }

    public companion object {
        /**
         * Parse a BIP-21 URI string into a Bip21URI object.
         *
         * Note that because we don't validate addresses in this library, it's possible to have a correctly formed
         * BIP-21 URI that does not have a valid address. Users should validate themselves the address for the network
         * they use.
         */
        public fun fromUri(input: String): Bip21URI {
            val uri = URI(input)
            require(uri.scheme?.lowercase() == "bitcoin") {
                "Invalid scheme '${input.take(8)}', expected 'bitcoin:'"
            }

            // We use the raw scheme specific part, which is still percent-encoded. The decoded version would turn an
            // encoded '&' or '=' inside a value into a separator before we get to split the query on them.
            // This recasts the nullable String? as String and allows us to not have to null-check on further calls
            // below. It's not clear when the schemeSpecificPart could be null (I've only ever seen it empty).
            val schemeSpecificPart: String =
                uri.rawSchemeSpecificPart
                    ?: throw InvalidURIException(
                        "Scheme specific part is null",
                    )

            // If the string contains a '?' character, we deconstruct it into the address part (before the '?' and the
            // query part (after the '?'). If it doesn't contain a '?', we return a Bip21URI with only the address.
            val parts: List<String> = schemeSpecificPart.split("?", limit = 2)
            val address: String = parts[0]
            val query: String? = parts.getOrNull(1)

            if (query == null) {
                return Bip21URI(address = schemeSpecificPart)
            }

            require(query.isNotEmpty()) { "'?' character indicates query part but the part is empty" }
            val parametersMap: Map<String, String> = buildParametersMap(query)

            val amount: Amount? = parametersMap["amount"]?.fromBitcoinIntoAmount()
            // The keys and values in the map are already percent-decoded.
            val label: Label? = parametersMap["label"]?.let { Label(it) }
            val message: Message? = parametersMap["message"]?.let { Message(it) }
            val lightning: Lightning? = parametersMap["lightning"]?.let { Lightning(it) }
            val pj: PayJoin? = parametersMap["pj"]?.let { PayJoin(it) }
            // The pjos parameter is special; this library only supports PayJoin V2, and therefore sets pjos=0 (false)
            // anytime the pj parameter is present. See above for more comments on this parameter.
            val other: List<OtherParameter>? =
                parametersMap
                    .filterKeys { it !in setOf("amount", "label", "message", "lightning", "pj", "pjos") }
                    .takeIf { it.isNotEmpty() }
                    ?.map { OtherParameter(it.key, it.value) }

            return Bip21URI(
                address = address,
                amount = amount,
                label = label,
                message = message,
                lightning = lightning,
                pj = pj,
                otherParameters = other,
            )
        }

        /**
         * Deconstruct the raw (still percent-encoded) query part into a map of percent-decoded key-value pairs. This
         * method also checks that there are no duplicate keys and throw if any are found because the later ones would
         * override the earlier ones.
         *
         * Keys and values are percent-decoded exactly once, after the query has been split on its '&' and '='
         * separators, so that encoded separators inside keys and values are kept as regular characters.
         */
        private fun buildParametersMap(query: String): Map<String, String> {
            val keys: List<String> = query.split("&").map { it.split("=", limit = 2)[0].decodeURLQueryComponent() }
            if (keys.size != keys.toSet().size) throw InvalidURIException("Invalid URI: duplicate parameter")

            // BIP-21 requires clients to reject the whole URI if it contains a required (req-) parameter they don't
            // implement. This library doesn't implement any, so any req- parameter makes the URI invalid.
            val requiredParameter: String? = keys.firstOrNull { it.startsWith("req-", ignoreCase = true) }
            if (requiredParameter != null) {
                throw InvalidURIException("Invalid URI: required parameter '$requiredParameter' is not supported")
            }

            return query.split("&").associate { part ->
                val keyValueElement = part.split("=", limit = 2)
                require(keyValueElement.size == 2) { "Invalid URI: parameter $part does not have a separator" }

                val key = keyValueElement[0].decodeURLQueryComponent()
                val value = keyValueElement[1].decodeURLQueryComponent()
                require(key.isNotEmpty()) { "Invalid URI: parameter $part has an empty key" }
                require(value.isNotEmpty()) { "Invalid URI: parameter $part has an empty value" }

                key to value
            }
        }
    }
}
