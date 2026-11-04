/*
 * Copyright 2023-2026 Kotlin Bitcoin Tools contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.kotlinbitcointools.bip21

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PercentEncodingTest {
    @Test
    fun `Unreserved characters are not encoded`() {
        val unreserved = "abcxyzABCXYZ0123456789-._~"
        assertEquals(unreserved, unreserved.percentEncode())
    }

    @Test
    fun `Reserved characters and spaces are encoded with uppercase hexadecimal digits`() {
        assertEquals(
            "%20%21%23%24%25%26%27%28%29%2A%2B%2C%2F%3A%3B%3D%3F%40%5B%5D",
            " !#$%&'()*+,/:;=?@[]".percentEncode(),
        )
    }

    @Test
    fun `Non-ASCII characters are encoded as UTF-8 bytes`() {
        assertEquals("%C3%BC", "ü".percentEncode())
        assertEquals("%E2%82%BF", "₿".percentEncode())
        assertEquals("%F0%9F%8D%9F", "🍟".percentEncode())
    }

    @Test
    fun `Decoding accepts uppercase and lowercase hexadecimal digits`() {
        assertEquals("a&b", "a%26b".percentDecode())
        assertEquals("₿", "%e2%82%bf".percentDecode())
    }

    @Test
    fun `Plus sign is not decoded as a space`() {
        assertEquals("1+1", "1+1".percentDecode())
    }

    @Test
    fun `Characters that are not percent-encoded are kept as they are`() {
        assertEquals("Fish Chips ₿", "Fish Chips ₿".percentDecode())
        assertEquals("Fish Chips ₿", "Fish%20Chips ₿".percentDecode())
    }

    @Test
    fun `Encoded strings decode back to the original`() {
        listOf("", "plain", "Fish & Chips = 100% ? #1 + more", "Ünïcødé and emoji ₿ 🍟", "%41").forEach { original ->
            assertEquals(original, original.percentEncode().percentDecode())
        }
    }

    @Test
    fun `Decoding rejects a percent sign that is not followed by two hexadecimal digits`() {
        listOf("100%", "100%4", "100%zz", "%g1").forEach { invalid ->
            val exception = assertFailsWith<IllegalArgumentException>(invalid) { invalid.percentDecode() }
            assertEquals(
                "Invalid percent-encoding in '$invalid': '%' must be followed by two hexadecimal digits",
                exception.message,
            )
        }
    }

    @Test
    fun `Decoding rejects bytes that are not valid UTF-8`() {
        // %FF is never valid in UTF-8, and %E2%82 is the start of a three-byte character without its last byte.
        listOf("%FF", "%E2%82").forEach { invalid ->
            val exception = assertFailsWith<IllegalArgumentException>(invalid) { invalid.percentDecode() }
            assertEquals(
                "Invalid percent-encoding in '$invalid': the decoded bytes are not valid UTF-8",
                exception.message,
            )
        }
    }
}
