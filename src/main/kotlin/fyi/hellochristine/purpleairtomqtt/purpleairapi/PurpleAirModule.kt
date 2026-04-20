package fyi.hellochristine.purpleairtomqtt.purpleairapi

import dagger.Module
import dagger.Provides
import de.jensklingenberg.ktorfit.Ktorfit
import fyi.hellochristine.purpleairtomqtt.model.Device
import io.ktor.client.HttpClient
import io.ktor.http.URLBuilder
import io.ktor.http.path
import io.ktor.http.takeFrom
import javax.inject.Singleton

@Module
class PurpleAirModule {
    // Device ID to PurpleAir API
    @Provides
    @Singleton
    fun providePurpleAirApi(
        devices: List<Device>,
        ktorClient: HttpClient,
    ): PurpleAirApiByDevice {
        return devices.associateBy(
            { it.id },
            { device ->
                val url = URLBuilder().takeFrom(device.host).apply {
                    if (pathSegments.isEmpty()) path("/")
                }.build()

                val ktorfit = Ktorfit.Builder()
                    .baseUrl(url.toString())
                    .httpClient(ktorClient)
                    .build()

                ktorfit.createPurpleAirApi()
            }
        )
    }
}
