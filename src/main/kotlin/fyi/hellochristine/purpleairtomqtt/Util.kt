package fyi.hellochristine.purpleairtomqtt

import com.google.common.io.Resources
import nl.altindag.ssl.model.KeyStoreHolder
import nl.altindag.ssl.util.KeyManagerUtils
import nl.altindag.ssl.util.TrustManagerUtils
import java.io.File
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManagerFactory

object Util {
    fun keyManagerFactory(keystoreFile: File, password: String?): KeyManagerFactory {
        val pwd = password?.toCharArray()
        val keystore = KeyStore.getInstance(keystoreFile, pwd)
        return KeyManagerUtils.createKeyManagerFactory(KeyManagerUtils.createKeyManager(KeyStoreHolder(keystore, pwd)))
    }

    fun trustManagerFactory(trustStoreFile: File, password: String?): TrustManagerFactory {
        val keystore = KeyStore.getInstance(trustStoreFile, password?.toCharArray())
        return TrustManagerUtils.createTrustManagerFactory(TrustManagerUtils.createTrustManager(keystore))
    }

    fun getResourceFile(path: String): File {
        return File(Resources.getResource(path).toURI())
    }
}
