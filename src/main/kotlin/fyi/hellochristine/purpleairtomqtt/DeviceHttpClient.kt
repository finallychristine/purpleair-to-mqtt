package fyi.hellochristine.purpleairtomqtt

import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.Provides
import com.google.inject.Singleton
import fyi.hellochristine.purpleairtomqtt.purpleairapi.DeviceResponse
import fyi.hellochristine.purpleairtomqtt.purpleairapi.JsonDecoder
import fyi.hellochristine.purpleairtomqtt.sensor.Sensor
import fyi.hellochristine.purpleairtomqtt.sensor.apiResponseToSensor
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.Marker
import io.github.oshai.kotlinlogging.withLoggingContext
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor


class DeviceHttpClientModule: AbstractModule() {
    private val logger = KotlinLogging.logger { }

    @Provides
    @Singleton
    fun provideOkClient(): OkHttpClient {
        val client =  OkHttpClient.Builder()
        val interceptor = HttpLoggingInterceptor(logger = { msg ->
            logger.trace { msg }
        })
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        client.addInterceptor(interceptor)

        return client.build()
    }
}

class DeviceHttpClient @Inject constructor(
    private val httpClient: OkHttpClient,
) {
    private val logger = KotlinLogging.logger { }

    fun query(d: Device): Observable<Sensor> {
        return Observable.defer {
            logger.debug { "Querying device '${d.describe()}'" }
            val request = Request.Builder()
                .url("${d.host}/json?live=true")
                .build()

            val responseContent = httpClient.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "Response was not successful, got ${response.code}" }
                val body = requireNotNull(response.body) { "Response must have a body"}
                JsonDecoder.decodeFromString<DeviceResponse>(body.string())
            }

            val sensor = apiResponseToSensor(d, responseContent)
            Observable.just(sensor)
        }
    }
}
