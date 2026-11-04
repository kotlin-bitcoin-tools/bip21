/*
 * Copyright 2023-2026 Kotlin Bitcoin Tools contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.kotlinbitcointools.bip21.parameters

import org.kotlinbitcointools.bip21.percentEncode

/** The keys of the parameters that Bip21URI handles itself. Any other key is stored as an [OtherParameter]. */
internal val BIP21_URI_PARAMETER_KEYS: Set<String> = setOf("amount", "label", "message", "lightning", "pj", "pjos")

/**
 * This class represents any other parameter not defined in the BIP-21 specification.
 *
 * @property key The key of the parameter.
 * @property value The value of the parameter.
 */
public data class OtherParameter(
    val key: String,
    val value: String,
) : Parameter {
    init {
        require(key.isNotEmpty()) { "Parameter key cannot be empty" }
        require(value.isNotEmpty()) { "Value of parameter '$key' cannot be empty" }
        // These parameters have their own properties on Bip21URI (pjos is written automatically when pj is set).
        // Allowing them here would write the key twice in the URI, or have it read back as the standard parameter.
        require(key !in BIP21_URI_PARAMETER_KEYS) {
            "Invalid parameter key '$key': this parameter is handled by Bip21URI directly"
        }
    }

    override fun encode(): String = "&${key.percentEncode()}=${value.percentEncode()}"
}
