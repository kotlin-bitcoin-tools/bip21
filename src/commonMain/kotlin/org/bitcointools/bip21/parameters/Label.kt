/*
 * Copyright 2023 kotlin-bitcoin-tools and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.bitcointools.bip21.parameters

import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.encodeURLQueryComponent

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
