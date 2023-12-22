/*
 * Copyright 2023 kotlin-bitcoin-tools and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.bitcointools.bip21.parameters

import org.bitcointools.bip21.parameters.Message
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageTest {
    @Test
    fun `Encode a message parameter from a string with spaces`() {
        val message = Message("Donation for project xyz")
        assertEquals("&message=Donation%20for%20project%20xyz", message.encode())
    }

    @Test
    fun `Build a message parameter from a sanitized string`() {
        val message = Message.decodeFrom("Donation%20for%20project%20xyz")
        assertEquals("Donation for project xyz", message.value)
    }
}
