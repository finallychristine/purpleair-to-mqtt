package fyi.hellochristine.purpleairtomqtt.homeassistant

object Mapper {
    fun toHomeAssistantSensors(sensor: fyi.hellochristine.purpleairtomqtt.model.Sensor): List<SensorWithValue> {
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


        // Note: could be nice to distinguish between sensor A & B
        val airReading = sensor.airQualityReadings.firstOrNull()

        val aqi = airReading?.let {
            getSensorWithValue(
                sensor = sensor,
                id = "aqi",
                name = "AQI",
                value = airReading.pm25Aqi,
                deviceClass = DeviceClass.AQI,
                unitOfMeasurement = null,
            )
        }

        val counts = airReading?.particulateCounts?.map { (diameter,count) ->
            getSensorWithValue(
                sensor = sensor,
                id = diameter.key() + "_count",
                name = diameter.description + " Count",
                value = count,
                deviceClass = null,
                unitOfMeasurement = UnitOfMeasurement.PARTICLE_DECILITER_COUNT,
                enabledByDefault = false,
            )
        } ?: emptyList()

        val pmReadings = airReading?.pmReadings
            ?.filter { it.methodology == sensor.place.methodology }
            ?.map { reading ->
                getSensorWithValue(
                    sensor = sensor,
                    id = reading.size.key(),
                    name = reading.size.description,
                    value = reading.amount,
                    deviceClass = reading.size.haDeviceClass,
                    unitOfMeasurement = UnitOfMeasurement.UG_M3,
                )
            } ?: emptyList()

        return listOfNotNull(temp, humidity, pressure, dewpoint, aqi) + counts + pmReadings
    }

    private fun getSensorWithValue(
        id: String,
        name: String,
        value: Any,
        sensor: fyi.hellochristine.purpleairtomqtt.model.Sensor,
        deviceClass: DeviceClass?,
        unitOfMeasurement: UnitOfMeasurement?,
        stateClass: StateClass? = StateClass.MEASUREMENT,
        enabledByDefault: Boolean = true,
    ): SensorWithValue {
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
                uniqueId = id,
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
