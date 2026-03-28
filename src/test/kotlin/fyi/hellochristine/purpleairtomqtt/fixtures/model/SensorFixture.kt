package fyi.hellochristine.purpleairtomqtt.fixtures.model

import fyi.hellochristine.purpleairtomqtt.model.AirQuality
import fyi.hellochristine.purpleairtomqtt.model.CelciusDegrees
import fyi.hellochristine.purpleairtomqtt.model.Channel
import fyi.hellochristine.purpleairtomqtt.model.Device
import fyi.hellochristine.purpleairtomqtt.model.PMReading
import fyi.hellochristine.purpleairtomqtt.model.PMReadingMethodology
import fyi.hellochristine.purpleairtomqtt.model.PMReadingSize
import fyi.hellochristine.purpleairtomqtt.model.ParticulateCountDiameter
import fyi.hellochristine.purpleairtomqtt.model.Place
import fyi.hellochristine.purpleairtomqtt.model.PolledDeviceInfo
import fyi.hellochristine.purpleairtomqtt.model.Sensor
import fyi.hellochristine.purpleairtomqtt.model.WeatherData

object SensorFixture {
    val place = Place.INDOOR
    const val aqi = 15

    const val deviceId = "f4:cf:a2:dd:9:d8"
    const val geo = "PurpleAir-9d8"
    const val softwareVersion = "7.2"
    const val hardwareDiscovered = "2.0+BME280+PMSX003-A"

    const val temperature: CelciusDegrees = 28.88
    const val dewpoint: CelciusDegrees = 5.55
    const val humidity = 23
    const val pressure = 1000.5

    // purpleair api uses ints
    const val temperatureF: Int = 84
    const val dewpointF: Int = 42


    fun createSensor(
        device: Device = DeviceFixture.createDevice(),
    ): Sensor {
        return Sensor(
            device = device,
            weatherData = createWeatherData(),
            place = place,
            airQualityReadings = setOf(createAirQualityReading()),
            polledDeviceInfo = createPolledDeviceInfo()
        )
    }

    fun createWeatherData(): WeatherData {
        return WeatherData(
            temperature = temperature,
            dewpoint = dewpoint,
            humidity = humidity,
            pressure = pressure,
        )
    }

    fun createAirQualityReading(): AirQuality {
        return AirQuality(
            channel = Channel.A,
            pm25Aqi = aqi,
            pmReadings = setOf(
                // Convention: PM size + 0.01 for ATM, PM size + 0.02 for CF1
                PMReading(size = PMReadingSize.PM1, methodology = PMReadingMethodology.CF1, amount = 1.02),
                PMReading(size = PMReadingSize.PM1, methodology = PMReadingMethodology.ATM, amount = 1.01),

                PMReading(size = PMReadingSize.PM2_5, methodology = PMReadingMethodology.CF1, amount = 2.52),
                PMReading(size = PMReadingSize.PM2_5, methodology = PMReadingMethodology.ATM, amount = 2.51),

                PMReading(size = PMReadingSize.PM10, methodology = PMReadingMethodology.CF1, amount = 10.02),
                PMReading(size = PMReadingSize.PM10, methodology = PMReadingMethodology.ATM, amount = 10.01),
            ),
            particulateCounts = mapOf(
                // Convention: just use the diameter directly
                ParticulateCountDiameter.P0_3 to 0.3,
                ParticulateCountDiameter.P0_5 to 0.5,
                ParticulateCountDiameter.P1_0 to 1.0,
                ParticulateCountDiameter.P2_5 to 2.5,
                ParticulateCountDiameter.P5_0 to 5.0,
                ParticulateCountDiameter.P10_0 to 10.0,
            )
        )
    }

    fun createPolledDeviceInfo(): PolledDeviceInfo {
        return PolledDeviceInfo(
            id = deviceId,
            friendlyId = geo,
            softwareVersion = softwareVersion,
            hardwareDiscovered = hardwareDiscovered,
        )
    }
}
