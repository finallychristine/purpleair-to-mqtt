package fyi.hellochristine.purpleairtomqtt.purpleairapi

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers


typealias PurpleAirApiByDevice = Map<String, @JvmSuppressWildcards PurpleAirApi>

interface PurpleAirApi {
    @Headers("Content-Type: application/json")
    @GET("json?live=true")
    suspend fun query(): DeviceResponse
}
