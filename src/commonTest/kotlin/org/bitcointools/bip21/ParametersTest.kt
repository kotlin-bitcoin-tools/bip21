package org.bitcointools.bip21

import kotlin.test.Test
import kotlin.test.assertEquals

class ParametersTest {
    @Test
    fun `Encode a label parameter from a string with spaces`() {
        val label = Label("Donation for project xyz")
        assertEquals("&label=Donation%20for%20project%20xyz", label.encode())
    }

    @Test
    fun `Build a label parameter from a sanitized string`() {
        val label = Label.decodeFrom("Donation%20for%20project%20xyz")
        assertEquals("Donation for project xyz", label.value)
    }

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

    @Test
    fun `Encode a lightning parameter from a bolt11 invoice`() {
        val lightning = Lightning("LNBC10U1P3PJ257PP5YZTKWJCZ5FTL5LAXKAV23ZMZEKAW37ZK6KMV80PK4XAEV5QHTZ7QDPDWD3XGER9WD5KWM36YPRX7U3QD36KUCMGYP282ETNV3SHJCQZPGXQYZ5VQSP5USYC4LK9CHSFP53KVCNVQ456GANH60D89REYKDNGSMTJ6YW3NHVQ9QYYSSQJCEWM5CJWZ4A6RFJX77C490YCED6PEMK0UPKXHY89CMM7SCT66K8GNEANWYKZGDRWRFJE69H9U5U0W57RRCSYSAS7GADWMZXC8C6T0SPJAZUP6")
        assertEquals(
            "&lightning=LNBC10U1P3PJ257PP5YZTKWJCZ5FTL5LAXKAV23ZMZEKAW37ZK6KMV80PK4XAEV5QHTZ7QDPDWD3XGER9WD5KWM36YPRX7U3QD36KUCMGYP282ETNV3SHJCQZPGXQYZ5VQSP5USYC4LK9CHSFP53KVCNVQ456GANH60D89REYKDNGSMTJ6YW3NHVQ9QYYSSQJCEWM5CJWZ4A6RFJX77C490YCED6PEMK0UPKXHY89CMM7SCT66K8GNEANWYKZGDRWRFJE69H9U5U0W57RRCSYSAS7GADWMZXC8C6T0SPJAZUP6",
            lightning.encode()
        )
    }
}
