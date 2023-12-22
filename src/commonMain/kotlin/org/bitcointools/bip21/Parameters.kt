/*
 * Copyright 2023 kotlin-bitcoin-tools and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.bitcointools.bip21

import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.encodeURLQueryComponent

/**
 * All parameters that can be included in a BIP-21 URI must have a key and a value, and this interface ensures that we
 * can always encode them to a string in the format that is required by the BIP-21 specification.
 *
 * Note that the BIP-21 specification requires strings for query components be sanitized to make them safe to include in
 * URIs. Moreover, the percent-encoded space (%20) must be used as per RFC 3986 instead of the sometimes-used '+'
 * character.
 */
public interface Parameter {
    public fun encode(): String
}

/**
 * The label parameter.
 *
 * @property value The value of the label.
 */
public data class Label(val value: String) : Parameter {
    override fun encode(): String = "&label=${value.encodeURLQueryComponent(encodeFull = true)}"

    public companion object {
        public fun decodeFrom(sanitizedString: String): Label = Label(sanitizedString.decodeURLQueryComponent())
    }
}

/**
 * The message parameter.
 *
 * @property value The value of the message.
 */
public data class Message(val value: String) : Parameter {
    override fun encode(): String = "&message=${value.encodeURLQueryComponent(encodeFull = true)}"

    public companion object {
        public fun decodeFrom(sanitizedString: String): Message = Message(sanitizedString.decodeURLQueryComponent())
    }
}

/**
 * The lightning parameter.
 *
 * @property pr The payment request.
 */
public data class Lightning(val pr: String) : Parameter {
    override fun encode(): String = "&lightning=${pr.encodeURLQueryComponent(encodeFull = true)}"
}

/**
 * This class represents any other parameter not defined in the BIP-21 specification.
 *
 * @property key The key of the parameter.
 * @property value The value of the parameter.
 */
public data class OtherParameter(val key: String, val value: String) : Parameter {
    override fun encode(): String = "&${key.encodeURLQueryComponent(encodeFull = true)}=${value.encodeURLQueryComponent(encodeFull = true)}"
}
