/*
 * Copyright 2023-2026 Kotlin Bitcoin Tools contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.kotlinbitcointools.bip21

private const val HEX_DIGITS: String = "0123456789ABCDEF"

/** The RFC 3986 unreserved characters. They are the only characters that never need to be percent-encoded. */
private fun isUnreserved(char: Char): Boolean =
    char in 'a'..'z' || char in 'A'..'Z' || char in '0'..'9' || char == '-' || char == '.' || char == '_' || char == '~'

/** Return the value of a hexadecimal digit, or -1 if the byte is not one. */
private fun hexDigitValue(byte: Byte): Int =
    when (val char = (byte.toInt() and 0xFF).toChar()) {
        in '0'..'9' -> char - '0'
        in 'A'..'F' -> char - 'A' + 10
        in 'a'..'f' -> char - 'a' + 10
        else -> -1
    }

/**
 * Percent-encode a string so that it can be used as a key or a value in a URI query (RFC 3986).
 *
 * Every character is encoded as UTF-8, and every byte that is not an unreserved character is written as '%' followed by
 * two uppercase hexadecimal digits. This includes the space (written as %20, never as '+') and all the characters that
 * have a meaning in a URI, like '&', '=', '?', '#', '%', and '+'.
 */
internal fun String.percentEncode(): String = buildString {
    for (byte in this@percentEncode.encodeToByteArray()) {
        val value: Int = byte.toInt() and 0xFF
        if (isUnreserved(value.toChar())) {
            append(value.toChar())
        } else {
            append('%')
            append(HEX_DIGITS[value shr 4])
            append(HEX_DIGITS[value and 0x0F])
        }
    }
}

/**
 * Decode a percent-encoded key or value from a URI query (RFC 3986).
 *
 * Each '%' followed by two hexadecimal digits (in either case) is replaced by the byte it encodes, and the resulting
 * bytes are read as UTF-8. Other characters are kept as they are. In particular, '+' stays a '+': it only means a space
 * in HTML form encoding, which BIP-21 URIs don't use.
 *
 * @throws IllegalArgumentException if a '%' is not followed by two hexadecimal digits, or if the decoded bytes are not
 *   valid UTF-8.
 */
internal fun String.percentDecode(): String {
    // We work on the UTF-8 bytes of the input so that characters that were not percent-encoded and the decoded bytes
    // end up in the same byte array. The '%' byte (0x25) can't appear inside a multi-byte UTF-8 character, so every '%'
    // byte we find really is a '%' character. Decoding never makes the input longer, so the output fits in an array of
    // the same size.
    val input: ByteArray = encodeToByteArray()
    val output = ByteArray(input.size)
    var outputSize = 0

    var index = 0
    while (index < input.size) {
        if (input[index] == '%'.code.toByte()) {
            val high: Int = if (index + 1 < input.size) hexDigitValue(input[index + 1]) else -1
            val low: Int = if (index + 2 < input.size) hexDigitValue(input[index + 2]) else -1
            require(high != -1 && low != -1) {
                "Invalid percent-encoding in '$this': '%' must be followed by two hexadecimal digits"
            }
            output[outputSize++] = ((high shl 4) or low).toByte()
            index += 3
        } else {
            output[outputSize++] = input[index]
            index++
        }
    }

    return try {
        output.decodeToString(startIndex = 0, endIndex = outputSize, throwOnInvalidSequence = true)
    } catch (e: CharacterCodingException) {
        throw IllegalArgumentException("Invalid percent-encoding in '$this': the decoded bytes are not valid UTF-8", e)
    }
}
