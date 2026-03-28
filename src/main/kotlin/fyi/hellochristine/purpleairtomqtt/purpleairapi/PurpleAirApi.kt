package fyi.hellochristine.purpleairtomqtt.purpleairapi

import com.google.inject.Inject
import fyi.hellochristine.purpleairtomqtt.model.Device
import fyi.hellochristine.purpleairtomqtt.model.Sensor
import io.github.oshai.kotlinlogging.KotlinLogging
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.Request

class PurpleAirApi @Inject constructor(
    private val httpClient: OkHttpClient,
) {
    private val logger = KotlinLogging.logger { }

    fun query(d: Device): Observable<Sensor> {
        return Observable.defer {
            val url = "${d.host}/json?live=true"
            logger.info { "Querying device" }
            logger.debug { "Issuing HTTP request to $url" }
            val request = Request.Builder()
                .url(url)
                .build()

            val responseContent = httpClient.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "Response was not successful, got ${response.code}" }
                val body = requireNotNull(response.body) { "Response must have a body"}
                JsonDecoder.decodeFromString<DeviceResponse>(body.string())
            }

            val sensor = Mapper.apiResponseToSensor(d, responseContent)
            Observable.just(sensor)
        }
    }
}
