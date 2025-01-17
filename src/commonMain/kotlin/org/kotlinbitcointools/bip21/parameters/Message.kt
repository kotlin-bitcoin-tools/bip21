/*
 * Copyright 2023-2025 Kotlin Bitcoin Tools contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.kotlinbitcointools.bip21.parameters

import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.encodeURLQueryComponent

/**
 * The message parameter.
 *
 * @property value The value of the message.
 */
public data class Message(val value: String) : Parameter {
    override fun encode(): String = "&message=${value.encodeURLQueryComponent(encodeFull = true)}"

    public companion object {
        public fun decodeFrom(uriString: String): Message =
            Message(uriString.decodeURLQueryComponent())
    }
}