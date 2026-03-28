package fyi.hellochristine.purpleairtomqtt.app

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class OkHttpClientModule: AbstractModule() {
    private val logger = KotlinLogging.logger { }

    @Provides
    @Singleton
    fun provideOkClient(): OkHttpClient {
        val client = OkHttpClient.Builder()
        val interceptor = HttpLoggingInterceptor(logger = { msg ->
            logger.trace { msg }
        })
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        client.addInterceptor(interceptor)

        return client.build()
    }
}
