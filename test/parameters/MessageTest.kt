/*
 * Copyright 2023-2026 Kotlin Bitcoin Tools contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.kotlinbitcointools.bip21.parameters

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MessageTest {
    @Test
    fun `Encode a message parameter from a string with spaces`() {
        val message = Message("Donation for project xyz")
        assertEquals("&message=Donation%20for%20project%20xyz", message.encode())
    }

    @Test
    fun `Message value cannot be empty`() {
        val exception = assertFailsWith<IllegalArgumentException> { Message("") }
        assertEquals("Message value cannot be empty", exception.message)
    }
}
