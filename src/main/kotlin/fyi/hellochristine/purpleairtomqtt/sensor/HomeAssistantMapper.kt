package fyi.hellochristine.purpleairtomqtt.sensor

import fyi.hellochristine.purpleairtomqtt.homeassistant.*

typealias HASensor = fyi.hellochristine.purpleairtomqtt.homeassistant.Sensor

fun toHomeAssistantSensors(sensor: Sensor): List<HASensorWithValue> {
    val device = getDevice(sensor)

    val temp = sensor.weatherData?.let {
        HASensorWithValue(
            id = SensorId.TEMPERATURE,
            value = sensor.weatherData.temperature,
            sensor = HASensor(
                name = SensorId.TEMPERATURE.description,
                deviceClass = DeviceClass.TEMPERATURE,
                unitOfMeasurement = UnitOfMeasurement.CELSIUS,
                stateTopic = getStateTopic(sensor, SensorId.TEMPERATURE),
                availabilityTopic = getAvailabilityTopic(sensor, SensorId.TEMPERATURE),
                uniqueId = SensorId.TEMPERATURE.mqttId,
                device = getDevice(sensor),
            )

        )
    }

    val humidity = sensor.weatherData?.let {
        HASensorWithValue(
            id = SensorId.TEMPERATURE,
            value = sensor.weatherData.humidity,

            deviceClass = DeviceClass.HUMIDITY,
            unitOfMeasurement = UnitOfMeasurement.PERCENTAGE,
            value = sensor.weatherData.humidity,
        )
    }

    val pressure = sensor.weatherData?.let {
        HASensor(
            key = "pressure",
            name = "Pressure",
            deviceClass = DeviceClass.PRESSURE,
            unitOfMeasurement = UnitOfMeasurement.MBAR,
            value = sensor.weatherData.pressure,
        )
    }

    // Note: could be nice to distinguish between sensor A & B
    val airReading = sensor.airQualityReadings.firstOrNull()

    val counts = airReading?.particulateCounts?.map { (diam,count) ->
        HASensor(
            key = diam.key(),
            name = diam.description,
            deviceClass = null,
            unitOfMeasurement = UnitOfMeasurement.PARTICLE_DECILITER_COUNT,
            value = count,
            enabledByDefault = false,
        )
    } ?: emptyList()

    val pmReadings = airReading?.pmReadings
        ?.filter { it.methodology == sensor.place.methodology }
        ?.map { reading ->
            HASensor(
                key = reading.size.key(),
                name = reading.size.description,
                deviceClass = reading.size.haDeviceClass,
                unitOfMeasurement = UnitOfMeasurement.UG_M3,
                value = reading.amount
            )
        } ?: emptyList()


    return listOfNotNull(temp, humidity, pressure) + counts + pmReadings
}

fun getDevice(sensor: Sensor): SensorDevice {
    return SensorDevice()
}

fun getAvailabilityTopic(
    sensor: Sensor,
    haSensorId: SensorId,
): String {
    return "purpleairtomqtt/${sensor.device.id}/${haSensorId.mqttId}/status"
}

fun getStateTopic(
    sensor: Sensor,
    haSensorId: SensorId,
): String {
    return "purpleairtomqtt/${sensor.device.id}/${haSensorId.mqttId}/state"
}
