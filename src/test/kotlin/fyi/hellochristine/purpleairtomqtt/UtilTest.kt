package fyi.hellochristine.purpleairtomqtt

import com.google.common.io.Resources
import nl.altindag.ssl.util.KeyManagerUtils
import nl.altindag.ssl.util.TrustManagerUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class UtilTest {
    @Test
    fun keyManagerFactory() {
        val f = Util.getResourceFile("ssl/client-keystore.p12")
        val factory = Util.keyManagerFactory(f, "password")
        val km = KeyManagerUtils.getKeyManager(factory)
        val key = km.getPrivateKey("client-keystore")
        assertNotNull(key)
    }

    @Test
    fun trustManagerFactory() {
        val f = Util.getResourceFile("ssl/client-truststore.p12")
        val factory = Util.trustManagerFactory(f, "password")
        val tm = TrustManagerUtils.getTrustManager(factory)
        val cert = tm.acceptedIssuers.first()
        assertNotNull(cert)
    }
}
