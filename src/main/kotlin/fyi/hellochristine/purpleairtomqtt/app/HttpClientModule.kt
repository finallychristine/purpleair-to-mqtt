package fyi.hellochristine.purpleairtomqtt.app

import dagger.Module
import dagger.Provides
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton


class HttpClientLogger: Logger {
    private val logger = KotlinLogging.logger { }

    override fun log(message: String) {
        logger.trace { message }
    }
}

@Module
class HttpClientModule {
    @Provides
    @Singleton
    fun provideKtorHttpClient(): HttpClient {
        return HttpClient {
            install(io.ktor.client.plugins.logging.Logging) {
                logger = HttpClientLogger()
                level = LogLevel.BODY
            }

            install(ContentNegotiation) {
                json(Json{
                    ignoreUnknownKeys = true
                })
            }
        }
    }
}
