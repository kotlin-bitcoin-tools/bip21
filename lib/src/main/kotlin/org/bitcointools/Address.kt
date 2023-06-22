/*
 * Copyright 2023 kotlin-bitcoin-tools and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */

package org.bitcointools

import fr.acinq.bitcoin.Bitcoin

/**
 * Represents a bitcoin address. This is really just a wrapper around the string value, but it provides
 * a way to test for the validity of the address given a specific [Network].
 */
public class Address(
    public val value: String,
    network: Network
) {
    init {
        // Throws with a meaningful error if the string cannot be parsed into an address that matches the network.
        Bitcoin.addressToPublicKeyScript(network.chainHash, value)
    }


    /**
     * Returns a string representation of the address.
     */
    override fun toString(): String {
        return value
    }
}
