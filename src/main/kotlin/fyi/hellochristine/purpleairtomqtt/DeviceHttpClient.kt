package fyi.hellochristine.purpleairtomqtt

import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.Provides
import com.google.inject.Singleton
import fyi.hellochristine.purpleairtomqtt.purpleairapi.DeviceResponse
import fyi.hellochristine.purpleairtomqtt.purpleairapi.JsonDecoder
import fyi.hellochristine.purpleairtomqtt.sensor.Sensor
import fyi.hellochristine.purpleairtomqtt.sensor.apiResponseToSensor
import okhttp3.OkHttpClient
import okhttp3.Request


class DeviceHttpClientModule: AbstractModule() {
    @Provides
    @Singleton
    fun provideOkClient(): OkHttpClient {
        return OkHttpClient()
    }
}

class DeviceHttpClient @Inject constructor(
    private val httpClient: OkHttpClient,
) {
    fun query(d: Device): Sensor? {
        val request = Request.Builder()
            .url("${d.host}/json?live=true")
            .build()


        val responseContent = httpClient.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "Response was not successful, got ${response.code}" }
            val body = requireNotNull(response.body) { "Response must have a body"}
            JsonDecoder.decodeFromString<DeviceResponse>(body.string())
        }

        val sensor = apiResponseToSensor(responseContent)
        return sensor
    }
}