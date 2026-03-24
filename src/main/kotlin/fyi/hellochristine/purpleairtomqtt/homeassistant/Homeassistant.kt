package fyi.hellochristine.purpleairtomqtt.homeassistant

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


enum class SensorId(
    val mqttId: String,
    val description: String,
) {
    TEMPERATURE(mqttId = "temperature", description = "Temperature"),
    HUMIDITY(mqttId = "humidity", description = "Humidity"),
}

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

@Serializable
enum class StateClass {
    @SerialName("measurement") MEASUREMENT,
}

@Serializable
data class Sensor(
    @SerialName("name") val name: String,
    @SerialName("device_cla") val deviceClass: DeviceClass?,
    @SerialName("unit_of_meas") val unitOfMeasurement: UnitOfMeasurement,
    @SerialName("stat_cla") val stateClass: StateClass? = StateClass.MEASUREMENT,
    @SerialName("stat_t") val stateTopic: String,
    @SerialName("avty_t") val availabilityTopic: String,
    @SerialName("uniq_id") val uniqueId: String,
    @SerialName("dev") val device: SensorDevice,
)

@Serializable
data class SensorDevice(
    @SerialName("ids") val ids: List<String>,
    @SerialName("name") val name: String,
    @SerialName("mdl") val model: String,
    @SerialName("configuration_url") val configurationUrl: String,
    @SerialName("sw_version") val softwareVersion: String,
    @SerialName("connections") val connections: List<List<String>>,
)

data class HASensorWithValue(
    val id: SensorId,
    val value: Any,
    val sensor: Sensor,

)
