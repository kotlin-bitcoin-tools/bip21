/*
 * Copyright 2023-2026 Kotlin Bitcoin Tools contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.kotlinbitcointools.bip21.parameters

import io.ktor.http.encodeURLQueryComponent

/**
 * The label parameter.
 *
 * @property value The value of the label.
 */
public data class Label(
    val value: String,
) : Parameter {
    init {
        require(value.isNotEmpty()) { "Label value cannot be empty" }
    }

    override fun encode(): String = "&label=${value.encodeURLQueryComponent(encodeFull = true)}"
}
