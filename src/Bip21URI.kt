/*
 * Copyright 2023-2026 Kotlin Bitcoin Tools contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.kotlinbitcointools.bip21

import org.kotlinbitcointools.bip21.parameters.Amount
import org.kotlinbitcointools.bip21.parameters.BIP21_URI_PARAMETER_KEYS
import org.kotlinbitcointools.bip21.parameters.Label
import org.kotlinbitcointools.bip21.parameters.Lightning
import org.kotlinbitcointools.bip21.parameters.Message
import org.kotlinbitcointools.bip21.parameters.OtherParameter
import org.kotlinbitcointools.bip21.parameters.PayJoin

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
    public val otherParameters: List<OtherParameter> = emptyList(),
) {
    init {
        // This library doesn't validate addresses for a specific network, but all bitcoin addresses (base58 and
        // bech32, in lowercase or uppercase) only use ASCII letters and digits. Rejecting anything else prevents
        // characters like '?', '&', or '#' in the address from changing the meaning of the encoded URI.
        require(address.isNotEmpty()) { "Invalid address: address cannot be empty" }
        require(address.all { it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' }) {
            "Invalid address '$address': addresses can only contain ASCII letters and digits"
        }

        // A key written twice in the URI would make it invalid, since fromUri rejects duplicate parameters.
        val otherKeys: List<String> = otherParameters.map { it.key }
        val duplicateKey: String? = otherKeys.firstOrNull { key -> otherKeys.count { it == key } > 1 }
        require(duplicateKey == null) { "Invalid other parameters: duplicate key '$duplicateKey'" }
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
                otherParameters.forEach { otherParameter -> builder.append(otherParameter.encode()) }
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
         *
         * @throws InvalidURIException if the input is not a valid BIP-21 URI. The underlying exception, if any, is
         *   available as its [cause][Throwable.cause].
         */
        public fun fromUri(input: String): Bip21URI {
            // The checks while parsing come from require() calls in this class and in the parameter classes
            // (IllegalArgumentException), and from this class (InvalidURIException). We wrap the first so that callers
            // only ever have to catch a single exception type.
            try {
                return parse(input)
            } catch (e: IllegalArgumentException) {
                throw InvalidURIException(e.message ?: "Invalid URI", e)
            }
        }

        private fun parse(input: String): Bip21URI {
            // The scheme is everything before the first ':'. It is case-insensitive, so "BITCOIN:" is accepted too.
            val scheme: String = input.substringBefore(":", missingDelimiterValue = "")
            require(scheme.equals("bitcoin", ignoreCase = true)) {
                "Invalid scheme '${input.take(8)}', expected 'bitcoin:'"
            }

            // The rest of the URI is still percent-encoded. We split it on its '?', '&' and '=' separators first and
            // only then percent-decode each key and value, so that an encoded '&' or '=' inside a value is not mistaken
            // for a separator.
            val schemeSpecificPart: String = input.substringAfter(":")

            // BIP-21 and BIP-321 require URIs to percent-encode every character that RFC 3986 doesn't allow in a query,
            // like spaces, '"', '<', '>', or '|'. They don't say what a parser should do when a URI doesn't. We
            // deliberately accept these characters as they are, because they have no meaning in the URI and can only be
            // read one way: "label=Fish Chips" can only mean the label "Fish Chips". Rejecting them would only make
            // payments fail for users scanning a QR code from a wallet that encodes values sloppily, and there is
            // nothing they can do about it. The URIs this library writes are always fully percent-encoded.
            //
            // We still reject the characters where accepting them would mean guessing:
            // - A '#' starts a fragment in a URI. BIP-21 URIs don't have fragments, so a '#' is most likely part of a
            //   value that wasn't percent-encoded. Either reading could be wrong ("label=Fish#Chips" could be the label
            //   "Fish" followed by a fragment, or the label "Fish#Chips"), so we reject the URI rather than guess.
            // - Control characters (like newlines or tabs) are never part of a valid URI. They are more likely to come
            //   from a corrupted or truncated input than from a value that wasn't encoded.
            require('#' !in schemeSpecificPart) {
                "Invalid URI: the '#' character is not allowed and must be percent-encoded as %23"
            }
            require(schemeSpecificPart.none { it.isISOControl() }) {
                "Invalid URI: control characters are not allowed and must be percent-encoded"
            }

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

            val amount: Amount? = parametersMap["amount"]?.let { Amount.decode(it) }
            // The keys and values in the map are already percent-decoded.
            val label: Label? = parametersMap["label"]?.let { Label(it) }
            val message: Message? = parametersMap["message"]?.let { Message(it) }
            val lightning: Lightning? = parametersMap["lightning"]?.let { Lightning(it) }
            val pj: PayJoin? = parametersMap["pj"]?.let { PayJoin(it) }
            // The pjos parameter is special; this library only supports PayJoin V2, and therefore sets pjos=0 (false)
            // anytime the pj parameter is present. See above for more comments on this parameter.
            val other: List<OtherParameter> =
                parametersMap.filterKeys { it !in BIP21_URI_PARAMETER_KEYS }.map { OtherParameter(it.key, it.value) }

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
            val keys: List<String> = query.split("&").map { it.split("=", limit = 2)[0].percentDecode() }
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

                val key = keyValueElement[0].percentDecode()
                val value = keyValueElement[1].percentDecode()
                require(key.isNotEmpty()) { "Invalid URI: parameter $part has an empty key" }
                require(value.isNotEmpty()) { "Invalid URI: parameter $part has an empty value" }

                key to value
            }
        }
    }
}
