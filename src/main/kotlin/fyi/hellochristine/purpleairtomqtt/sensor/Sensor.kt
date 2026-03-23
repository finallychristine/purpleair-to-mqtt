package fyi.hellochristine.purpleairtomqtt.sensor

import fyi.hellochristine.purpleairtomqtt.Device
import fyi.hellochristine.purpleairtomqtt.homeassistant.DeviceClass
import java.text.DecimalFormat

typealias CelciusDegrees = Double
typealias Micrometers = Double

private val PM_FORMAT = DecimalFormat("##.#")

@Target(AnnotationTarget.FIELD)
annotation class RequiredHardware(val hardware: Hardware)

enum class Hardware(val id: String) {
    BME("BME"),
    BME68X("BME68X"),
    PMSX003_A("PMSX003-A"),
    PMSX003_B("PMSX003-B"),
}

enum class Channel {
    @RequiredHardware(Hardware.PMSX003_A) A,
    @RequiredHardware(Hardware.PMSX003_B) B,
}

enum class PMReadingMethodology {
    /** CF=1 estimation of density. Indoor sensors display CF1 data. */
    CF1,

    /** ATM estimation of density. Outdoor sensors display ATM data. */
    ATM
}

/** Indoor and outdoor sensors, in this context, are determined by how the sensor is registered. */
enum class Place(val methodology: PMReadingMethodology) {
    INDOOR(PMReadingMethodology.CF1),
    OUTDOOR(PMReadingMethodology.ATM),
}

/** Aparticulate matter (PM) reading matching [size] or *less* */
enum class PMReadingSize(
    val size: Micrometers,
    val haDeviceClass: DeviceClass,
    val description: String = "PM" + PM_FORMAT.format(size),
) {
    PM1(1.0, DeviceClass.PM1),
    PM2_5(2.5, DeviceClass.PM25),
    PM10(10.0, DeviceClass.PM10);

    fun key() = name.lowercase() + "_mass_concentration"
}

/** A particulate count matching [size] or *greater* */
enum class ParticulateCountDiameter(
    val size: Micrometers,
    val description: String = "PM" + PM_FORMAT.format(size) + " count concentration",
) {
    P0_3(0.3),
    P0_5(0.5),
    P1_0(1.0),
    P2_5(2.5),
    P5_0(5.0),
    P10_0(10.0);

    fun key() = name.lowercase() + "_count_concentration"
}

data class Sensor(
    val device: Device,
    val weatherData: WeatherData?,
    val place: Place,
    val airQualityReadings: List<AirQuality>,
)

data class WeatherData(
    val temperature: CelciusDegrees,
    val dewpoint: CelciusDegrees,
    val humidity: Int,
    val pressure: Double,
)

data class AirQuality(
    val channel: Channel,
    /** US EPA PM2.5 AQI */
    val pm25Aqi: Int,
    val pmReadings: List<PMReading>,
    val particulateCounts: Map<ParticulateCountDiameter,Double>,
)

/**
 * Particulate Matter reading
 */
data class PMReading(
    val size: PMReadingSize,
    val methodology: PMReadingMethodology,
    val amount: Double,
)
