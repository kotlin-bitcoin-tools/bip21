package org.bitcointools

import kotlin.test.Test

class AddressTest {
    @Test
    fun `Mainnet address is created from bech32 string`() {
        Address("bc1q0xcqpzrky6eff2g52qdye53xkk9jxkvrh6yhyw", Network.MAINNET)
    }

    @Test
    fun `Testnet address is created from bech32 string`() {
        Address("tb1ql7w62elx9ucw4pj5lgw4l028hmuw80sndtntxt", Network.TESTNET)
    }

    @Test
    fun `Signet address is created from bech32 string`() {
        Address("tb1pwzv7fv35yl7ypwj8w7al2t8apd6yf4568cs772qjwper74xqc99sk8x7tk", Network.SIGNET)
    }

    @Test
    fun `Regtest address is created from bech32 string`() {
        Address("bcrt1q9vkmujggvzs0rd4z6069v3v0jucje7ua7ap308", Network.REGTEST)
    }
}
