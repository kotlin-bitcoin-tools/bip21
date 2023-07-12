/*
 * Copyright 2023 kotlin-bitcoin-tools and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */

package org.bitcointools.bip21

import fr.acinq.bitcoin.ByteVector32
import fr.acinq.bitcoin.Block

/**
 * This type is passed to the [Address] constructor to specify the network that the address is for and is used by
 * the fr.acinq.bitcoin library to determine the chain hash for the network.
 */
public enum class Network {
    MAINNET { override val chainHash: ByteVector32 = Block.LivenetGenesisBlock.hash },
    TESTNET { override val chainHash: ByteVector32 = Block.TestnetGenesisBlock.hash },
    REGTEST { override val chainHash: ByteVector32 = Block.RegtestGenesisBlock.hash },
    SIGNET { override val chainHash: ByteVector32 = Block.SignetGenesisBlock.hash };

    public abstract val chainHash: ByteVector32
}