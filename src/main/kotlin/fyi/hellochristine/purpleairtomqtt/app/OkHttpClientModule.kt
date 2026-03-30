package fyi.hellochristine.purpleairtomqtt.app

import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

@Module
class OkHttpClientModule {
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
