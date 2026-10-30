/*
 * Copyright 2023-2026 Kotlin Bitcoin Tools contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.kotlinbitcointools.bip21.parameters

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OtherParameterTest {
    @Test
    fun `Encode a random parameter`() {
        val parameter = OtherParameter("random", "value")
        assertEquals("&random=value", parameter.encode())
    }

    @Test
    fun `Parameter key and value cannot be empty`() {
        val emptyKey = assertFailsWith<IllegalArgumentException> { OtherParameter("", "value") }
        assertEquals("Parameter key cannot be empty", emptyKey.message)

        val emptyValue = assertFailsWith<IllegalArgumentException> { OtherParameter("random", "") }
        assertEquals("Value of parameter 'random' cannot be empty", emptyValue.message)
    }

    @Test
    fun `Parameter key cannot be one of the parameters handled by Bip21URI`() {
        listOf("amount", "label", "message", "lightning", "pj", "pjos").forEach { key ->
            val exception = assertFailsWith<IllegalArgumentException>(key) { OtherParameter(key, "value") }
            assertEquals(
                "Invalid parameter key '$key': this parameter is handled by Bip21URI directly",
                exception.message,
            )
        }
    }
}
