package fyi.hellochristine.purpleairtomqtt.sensor

import fyi.hellochristine.purpleairtomqtt.Device
import fyi.hellochristine.purpleairtomqtt.homeassistant.DeviceClass
import fyi.hellochristine.purpleairtomqtt.homeassistant.UnitOfMeasurement

typealias HASensor = fyi.hellochristine.purpleairtomqtt.homeassistant.Sensor<Any>

fun toHomeAssistantSensors(d: Device, sensor: Sensor): List<HASensor> {
    val temp = sensor.weatherData?.let {
        HASensor(
            key = "temperature",
            name = "Temperature",
            deviceClass = DeviceClass.TEMPERATURE,
            unitOfMeasurement = UnitOfMeasurement.CELSIUS,
            value = sensor.weatherData.temperature,
        )
    }

    val humidity = sensor.weatherData?.let {
        HASensor(
            key = "humidity",
            name = "Humidity",
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