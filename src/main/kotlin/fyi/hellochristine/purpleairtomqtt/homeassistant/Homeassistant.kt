package fyi.hellochristine.purpleairtomqtt.homeassistant

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DeviceClass {
    @SerialName("temperature") TEMPERATURE,
    @SerialName("humidity") HUMIDITY,
    @SerialName("pressure") PRESSURE,
    @SerialName("pm1") PM1,
    @SerialName("pm25") PM25,
    @SerialName("pm10") PM10,
}

@Serializable
enum class UnitOfMeasurement {
    @SerialName("°C") CELSIUS,
    @SerialName("%") PERCENTAGE,
    /** Particle count per deciliter */
    @SerialName("particles/100mL") PARTICLE_DECILITER_COUNT,
    @SerialName("aqi") AQI,
    /** Micrograms per cubic meter of air. Measurement of mass */
    @SerialName("μg/m³") UG_M3,
    @SerialName("mbar") MBAR,
}

data class Sensor<T: Any>(
    val key: String,
    val name: String,
    val deviceClass: DeviceClass?,
    val unitOfMeasurement: UnitOfMeasurement,
    val value: T,
    val enabledByDefault: Boolean = true,
)