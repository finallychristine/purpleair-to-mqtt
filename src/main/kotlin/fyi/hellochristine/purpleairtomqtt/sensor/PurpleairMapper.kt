package fyi.hellochristine.purpleairtomqtt.sensor

import fyi.hellochristine.purpleairtomqtt.purpleairapi.DeviceResponse

fun apiResponseToSensor(response: DeviceResponse): Sensor {
    return Sensor(
        weatherData = parseWeather(response),
        place = parsePlace(response.place),
        airQualityReadings = listOfNotNull(
            parseChannel(
                channel = Channel.A,
                aqi = response.pm25_aqi,
                pmReadings = listOfNotNull(
                    parsePmReading(PMReadingSize.PM1, PMReadingMethodology.CF1, response.pm1_0_cf_1),
                    parsePmReading(PMReadingSize.PM1, PMReadingMethodology.ATM, response.pm1_0_atm),
                    parsePmReading(PMReadingSize.PM2_5, PMReadingMethodology.CF1, response.pm2_5_cf_1),
                    parsePmReading(PMReadingSize.PM2_5, PMReadingMethodology.ATM, response.pm2_5_atm),
                    parsePmReading(PMReadingSize.PM10, PMReadingMethodology.CF1, response.pm10_0_cf_1),
                    parsePmReading(PMReadingSize.PM10, PMReadingMethodology.ATM, response.pm10_0_atm),
                ),
                particulateCounts = listOfNotNull(
                    parseParticulateCounts(ParticulateCountDiameter.P0_3, response.p_0_3_um),
                    parseParticulateCounts(ParticulateCountDiameter.P0_5, response.p_0_5_um),
                    parseParticulateCounts(ParticulateCountDiameter.P1_0, response.p_1_0_um),
                    parseParticulateCounts(ParticulateCountDiameter.P2_5, response.p_2_5_um),
                    parseParticulateCounts(ParticulateCountDiameter.P5_0, response.p_5_0_um),
                    parseParticulateCounts(ParticulateCountDiameter.P10_0, response.p_10_0_um),
                )
            ),
            parseChannel(
                channel = Channel.B,
                aqi = response.pm25_aqi_b,
                pmReadings = listOfNotNull(
                    parsePmReading(PMReadingSize.PM1, PMReadingMethodology.CF1, response.pm1_0_cf_1_b),
                    parsePmReading(PMReadingSize.PM1, PMReadingMethodology.ATM, response.pm1_0_atm_b),
                    parsePmReading(PMReadingSize.PM2_5, PMReadingMethodology.CF1, response.pm2_5_cf_1_b),
                    parsePmReading(PMReadingSize.PM2_5, PMReadingMethodology.ATM, response.pm2_5_atm_b),
                    parsePmReading(PMReadingSize.PM10, PMReadingMethodology.CF1, response.pm10_0_cf_1_b),
                    parsePmReading(PMReadingSize.PM10, PMReadingMethodology.ATM, response.pm10_0_atm_b),
                ),
                particulateCounts = listOfNotNull(
                    parseParticulateCounts(ParticulateCountDiameter.P0_3, response.p_0_3_um_b),
                    parseParticulateCounts(ParticulateCountDiameter.P0_5, response.p_0_5_um_b),
                    parseParticulateCounts(ParticulateCountDiameter.P1_0, response.p_1_0_um_b),
                    parseParticulateCounts(ParticulateCountDiameter.P2_5, response.p_2_5_um_b),
                    parseParticulateCounts(ParticulateCountDiameter.P5_0, response.p_5_0_um_b),
                    parseParticulateCounts(ParticulateCountDiameter.P10_0, response.p_10_0_um_b),
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
    return (f.toDouble() - 32) / 1.8
}