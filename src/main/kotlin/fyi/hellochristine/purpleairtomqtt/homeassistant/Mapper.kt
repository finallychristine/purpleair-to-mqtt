package fyi.hellochristine.purpleairtomqtt.homeassistant

import fyi.hellochristine.purpleairtomqtt.model.AirQuality
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt

object Mapper {
    fun toHomeAssistantSensors(sensor: fyi.hellochristine.purpleairtomqtt.model.Sensor): List<SensorWithValue<out Number>> {
        val temp = sensor.weatherData?.let {
            getSensorWithValue(
                sensor = sensor,
                id = "temperature",
                name = "Temperature",
                value = sensor.weatherData.temperature,
                deviceClass = DeviceClass.TEMPERATURE,
                unitOfMeasurement = UnitOfMeasurement.CELSIUS,
            )
        }

        val humidity = sensor.weatherData?.let {
            getSensorWithValue(
                sensor = sensor,
                id = "humidity",
                name = "Humidity",
                value = sensor.weatherData.humidity,
                deviceClass = DeviceClass.HUMIDITY,
                unitOfMeasurement = UnitOfMeasurement.PERCENTAGE,
            )
        }

        val pressure = sensor.weatherData?.let {
            getSensorWithValue(
                sensor = sensor,
                id = "pressure",
                name = "Pressure",
                value = sensor.weatherData.pressure,
                deviceClass = DeviceClass.PRESSURE,
                unitOfMeasurement = UnitOfMeasurement.MBAR,
            )
        }

        val dewpoint = sensor.weatherData?.let {
            getSensorWithValue(
                sensor = sensor,
                id = "dewpoint",
                name = "Dewpoint",
                value = sensor.weatherData.dewpoint,
                deviceClass = DeviceClass.TEMPERATURE,
                unitOfMeasurement = UnitOfMeasurement.CELSIUS,
            )
        }

        val voc = sensor.vocReading?.let {
            getSensorWithValue(
                sensor = sensor,
                id = "tvoc",
                name = "TVOC",
                value = sensor.vocReading,
                deviceClass = DeviceClass.VOC_PARTS,
                unitOfMeasurement = UnitOfMeasurement.PPB,
            )
        }

        fun <T: Any>groupIntoChannels(getter: (AirQuality) -> T?): List<T>? {
            val list = sensor.airQualityReadings.mapNotNull(getter)
            if (list.isEmpty()) {
                return null
            }
            return list
        }

        fun <T: Any, K: Any>associateIntoChannels(getter: (AirQuality) -> Collection<T>?, by: (T) -> K): Map<K, List<T>>? {
            val list = sensor.airQualityReadings.mapNotNull(getter)
            if (list.isEmpty()) {
                return null
            }

            val associated = list.map { it.groupBy(by) }
            val allKeys = associated.flatMap { it.keys }.distinct()

            return allKeys
                .groupBy({it}, {key -> associated.map { a -> a[key] ?: emptyList() } })
                .mapValues { it.value.flatten().flatten() }
        }


        val aqi = groupIntoChannels{ it.pm25Aqi }?.let { readings ->
            getSensorWithValue(
                sensor = sensor,
                id = "aqi",
                name = "AQI",
                value = readings.average().roundToInt(),
                deviceClass = DeviceClass.AQI,
                unitOfMeasurement = null,
            )
        }

        val counts = associateIntoChannels({ it.particulateCounts.entries }, { it.key })
            ?.map { (diameter, entries) ->
                getSensorWithValue(
                    sensor = sensor,
                    id = diameter.key() + "_count",
                    name = diameter.description + " Count",
                    value = entries.map { it.value }.average().roundToInt(),
                    deviceClass = null,
                    unitOfMeasurement = UnitOfMeasurement.PARTICLE_DECILITER_COUNT,
                    enabledByDefault = false,
                )
            } ?: emptyList()

        val pmReadings = associateIntoChannels(
                getter = { aq -> aq.pmReadings.filter { reading -> reading.methodology == sensor.place.methodology } },
                by = { it.size })
            ?.map { (size, readings) ->
                getSensorWithValue(
                    sensor = sensor,
                    id = size.key(),
                    name = size.description,
                    value = readings.map { it.amount }.average().roundTo(2),
                    deviceClass = size.haDeviceClass,
                    unitOfMeasurement = UnitOfMeasurement.UG_M3,
                )
            } ?: emptyList()

        return listOfNotNull(temp, humidity, pressure, dewpoint, aqi, voc) + counts + pmReadings
    }

    private fun <T: Number>getSensorWithValue(
        id: String,
        name: String,
        value: T,
        sensor: fyi.hellochristine.purpleairtomqtt.model.Sensor,
        deviceClass: DeviceClass?,
        unitOfMeasurement: UnitOfMeasurement?,
        stateClass: StateClass? = StateClass.MEASUREMENT,
        enabledByDefault: Boolean = true,
    ): SensorWithValue<T> {
        return SensorWithValue(
            value = value,
            haDiscoveryTopic = getDiscoveryTopic(sensor, id),
            sensor = Sensor(
                name = name,
                deviceClass = deviceClass,
                unitOfMeasurement = unitOfMeasurement,
                stateClass = stateClass,
                stateTopic = getStateTopic(sensor, id),
                availabilityTopic = getAvailabilityTopic(sensor, id),
                uniqueId = "purpleair_${sensor.device.id}_${id}",
                enabledByDefault = enabledByDefault,
                device = Device(
                    ids = listOf(
                        "purpleair-to-mqtt--${sensor.device.id}",
                        sensor.polledDeviceInfo.id,
                    ),
                    name = sensor.polledDeviceInfo.friendlyId,
                    model = sensor.polledDeviceInfo.hardwareDiscovered,
                    configurationUrl = sensor.device.host,
                    softwareVersion = sensor.polledDeviceInfo.softwareVersion,
                    connections = listOf(
                        listOf("mac", sensor.polledDeviceInfo.id)
                    ),
                ),
            )
        )
    }

    fun getAvailabilityTopic(
        sensor: fyi.hellochristine.purpleairtomqtt.model.Sensor,
        id: String,
    ): String {
        return "purpleairtomqtt/${sensor.device.id}/${id}/status"
    }

    fun getStateTopic(
        sensor: fyi.hellochristine.purpleairtomqtt.model.Sensor,
        id: String,
    ): String {
        return "purpleairtomqtt/${sensor.device.id}/${id}/state"
    }

    fun getDiscoveryTopic(
        sensor: fyi.hellochristine.purpleairtomqtt.model.Sensor,
        id: String,
    ): String {
        return "homeassistant/sensor/purpleairtomqtt-${sensor.device.id}-${id}/config"
    }
}

fun Double.roundTo(decimals: Int): Double {
    val factor = 10.0.pow(decimals)
    return round(this * factor) / factor

}
