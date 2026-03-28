package fyi.hellochristine.purpleairtomqtt.purpleairapi

import fyi.hellochristine.purpleairtomqtt.model.Device
import fyi.hellochristine.purpleairtomqtt.model.AirQuality
import fyi.hellochristine.purpleairtomqtt.model.Channel
import fyi.hellochristine.purpleairtomqtt.model.PMReading
import fyi.hellochristine.purpleairtomqtt.model.PMReadingMethodology
import fyi.hellochristine.purpleairtomqtt.model.PMReadingSize
import fyi.hellochristine.purpleairtomqtt.model.ParticulateCountDiameter
import fyi.hellochristine.purpleairtomqtt.model.Place
import fyi.hellochristine.purpleairtomqtt.model.PolledDeviceInfo
import fyi.hellochristine.purpleairtomqtt.model.Sensor
import fyi.hellochristine.purpleairtomqtt.model.WeatherData

fun apiResponseToSensor(device: Device, response: DeviceResponse): Sensor {
    return Sensor(
        device = device,
        weatherData = parseWeather(response),
        place = parsePlace(response.place),
        polledDeviceInfo = parsePolledDeviceInfo(response),
        airQualityReadings = listOfNotNull(
            parseChannel(
                channel = Channel.A,
                aqi = response.pm25_aqi,
                pmReadings = listOfNotNull(
                    parsePmReading(
                        PMReadingSize.PM1,
                        PMReadingMethodology.CF1,
                        response.pm1_0_cf_1
                    ),
                    parsePmReading(
                        PMReadingSize.PM1,
                        PMReadingMethodology.ATM,
                        response.pm1_0_atm
                    ),
                    parsePmReading(
                        PMReadingSize.PM2_5,
                        PMReadingMethodology.CF1,
                        response.pm2_5_cf_1
                    ),
                    parsePmReading(
                        PMReadingSize.PM2_5,
                        PMReadingMethodology.ATM,
                        response.pm2_5_atm
                    ),
                    parsePmReading(
                        PMReadingSize.PM10,
                        PMReadingMethodology.CF1,
                        response.pm10_0_cf_1
                    ),
                    parsePmReading(
                        PMReadingSize.PM10,
                        PMReadingMethodology.ATM,
                        response.pm10_0_atm
                    ),
                ),
                particulateCounts = listOfNotNull(
                    parseParticulateCounts(
                        ParticulateCountDiameter.P0_3,
                        response.p_0_3_um
                    ),
                    parseParticulateCounts(
                        ParticulateCountDiameter.P0_5,
                        response.p_0_5_um
                    ),
                    parseParticulateCounts(
                        ParticulateCountDiameter.P1_0,
                        response.p_1_0_um
                    ),
                    parseParticulateCounts(
                        ParticulateCountDiameter.P2_5,
                        response.p_2_5_um
                    ),
                    parseParticulateCounts(
                        ParticulateCountDiameter.P5_0,
                        response.p_5_0_um
                    ),
                    parseParticulateCounts(
                        ParticulateCountDiameter.P10_0,
                        response.p_10_0_um
                    ),
                )
            ),
            parseChannel(
                channel = Channel.B,
                aqi = response.pm25_aqi_b,
                pmReadings = listOfNotNull(
                    parsePmReading(
                        PMReadingSize.PM1,
                        PMReadingMethodology.CF1,
                        response.pm1_0_cf_1_b
                    ),
                    parsePmReading(
                        PMReadingSize.PM1,
                        PMReadingMethodology.ATM,
                        response.pm1_0_atm_b
                    ),
                    parsePmReading(
                        PMReadingSize.PM2_5,
                        PMReadingMethodology.CF1,
                        response.pm2_5_cf_1_b
                    ),
                    parsePmReading(
                        PMReadingSize.PM2_5,
                        PMReadingMethodology.ATM,
                        response.pm2_5_atm_b
                    ),
                    parsePmReading(
                        PMReadingSize.PM10,
                        PMReadingMethodology.CF1,
                        response.pm10_0_cf_1_b
                    ),
                    parsePmReading(
                        PMReadingSize.PM10,
                        PMReadingMethodology.ATM,
                        response.pm10_0_atm_b
                    ),
                ),
                particulateCounts = listOfNotNull(
                    parseParticulateCounts(
                        ParticulateCountDiameter.P0_3,
                        response.p_0_3_um_b
                    ),
                    parseParticulateCounts(
                        ParticulateCountDiameter.P0_5,
                        response.p_0_5_um_b
                    ),
                    parseParticulateCounts(
                        ParticulateCountDiameter.P1_0,
                        response.p_1_0_um_b
                    ),
                    parseParticulateCounts(
                        ParticulateCountDiameter.P2_5,
                        response.p_2_5_um_b
                    ),
                    parseParticulateCounts(
                        ParticulateCountDiameter.P5_0,
                        response.p_5_0_um_b
                    ),
                    parseParticulateCounts(
                        ParticulateCountDiameter.P10_0,
                        response.p_10_0_um_b
                    ),
                )
            ),
        )
    )
}

private fun parseChannel(
    channel: Channel,
    aqi: Int?,
    pmReadings: List<PMReading>,
    particulateCounts: List<Pair<ParticulateCountDiameter, Double>>
): AirQuality? {
    if (pmReadings.isEmpty() || particulateCounts.isEmpty() || aqi == null) return null

    return AirQuality(
        channel = channel,
        pm25Aqi = aqi,
        pmReadings = pmReadings,
        particulateCounts = particulateCounts.toMap()
    )
}

private fun parseParticulateCounts(size: ParticulateCountDiameter, count: Double?): Pair<ParticulateCountDiameter, Double>? {
    count ?: return null

    return Pair(size, count)
}

private fun parsePmReading(size: PMReadingSize, methodology: PMReadingMethodology, amount: Double?): PMReading? {
    if (amount == null) return null
    return PMReading(
        size = size,
        methodology = methodology,
        amount = amount,
    )
}

private fun parseWeather(response: DeviceResponse): WeatherData? {
    val tempF = response.tempF ?: return null
    val dewF = response.dewpointF ?: return null

    return WeatherData(
        temperature = fToC(tempF),
        dewpoint = fToC(dewF),
        humidity = response.humidity ?: return null,
        pressure = response.pressure ?: return null,
    )
}

private fun parsePlace(place: String): Place {
    return when(place) {
        "inside" -> Place.INDOOR
        "outside" -> Place.OUTDOOR
        else -> throw IllegalArgumentException("Unknown place '$place'")
    }
}

private fun fToC(f: Int): Double {
    val precision = 100.0
    val c = (f.toDouble() - 32) / 1.8
    val cInt = (c * precision).toInt()
    return cInt / precision
}

private fun parsePolledDeviceInfo(response: DeviceResponse): PolledDeviceInfo {
    return PolledDeviceInfo(
        id = response.sensorId,
        friendlyId = response.geo,
        softwareVersion = response.firmwareVersion,
        hardwareDiscovered = response.hardwareDiscovered,
    )
}
