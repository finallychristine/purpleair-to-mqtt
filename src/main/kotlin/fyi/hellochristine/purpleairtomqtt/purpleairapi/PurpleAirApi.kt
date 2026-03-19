package fyi.hellochristine.purpleairtomqtt.purpleairapi

import fyi.hellochristine.purpleairtomqtt.sensor.Hardware
import fyi.hellochristine.purpleairtomqtt.sensor.RequiredHardware
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val JsonDecoder = Json {
    ignoreUnknownKeys = true
}

/**
 * See also: https://community.purpleair.com/t/sensor-json-documentation/6917
 */
@Serializable
data class DeviceResponse(
    ///// Universal Fields

    /** The Device ID for the sensor. This is identical to the MAC address, but with leading 0’s omitted in each octet */
    @SerialName("SensorId") val sensorId: String,
    /** This is the name of the PurpleAir-**** WiFI network displayed for device setup */
    @SerialName("Geo") val geo: String,
    /** Whether the sensor is registered as indoor or outdoor */
    @SerialName("place") val place: String,
    /** A label indicating all hardware the device board can see. This includes things such as the laser counters and BME environmental sensor */
    @SerialName("hardwarediscovered") val hardwareDiscovered: String,

    ///// BME

    /** The temperature measured in Fahrenheit. This is uncorrected */
    @SerialName("current_temp_f")
    @RequiredHardware(Hardware.BME)
    val tempF: Int? = null,

    /** The relative humidity as measured by the device. This is uncorrected */
    @SerialName("current_humidity")
    @RequiredHardware(Hardware.BME)
    val humidity: Int? = null,

    /** The dewpoint as measured by the device */
    @SerialName("current_dewpoint_f")
    @RequiredHardware(Hardware.BME)
    val dewpointF: Int? = null,

    /** The barometric pressure measured in millibar */
    @SerialName("pressure")
    @RequiredHardware(Hardware.BME)
    val pressure: Double? = null,

    ///// PMSX003_A

    /** US EPA PM2.5 AQI as measured by channel A */
    @SerialName("pm2.5_aqi")
    @RequiredHardware(Hardware.PMSX003_A)
    val pm25_aqi: Int? = null,
    /** PM1 readings from channel A using the CF=1 estimation of density */
    @RequiredHardware(Hardware.PMSX003_A) val pm1_0_cf_1: Double? = null,
    /** Channel A 0.3-micrometer and larger particle counts per deciliter of air */
    @RequiredHardware(Hardware.PMSX003_A) val p_0_3_um: Double? = null,
    /** PM2.5 readings from channel A using the CF=1 estimation of density */
    @RequiredHardware(Hardware.PMSX003_A) val pm2_5_cf_1: Double? = null,
    /** Channel A 0.5-micrometer and larger particle counts per deciliter of air */
    @RequiredHardware(Hardware.PMSX003_A) val p_0_5_um: Double? = null,
    /** PM10 readings from channel A using the CF=1 estimation of density */
    @RequiredHardware(Hardware.PMSX003_A) val pm10_0_cf_1: Double? = null,
    /** Channel A 1.0-micrometer and larger particle counts per deciliter of air */
    @RequiredHardware(Hardware.PMSX003_A) val p_1_0_um: Double? = null,
    /** PM1 readings from channel A using the ATM estimation of density */
    @RequiredHardware(Hardware.PMSX003_A) val pm1_0_atm: Double? = null,
    /** Channel A 2.5-micrometer and larger particle counts per deciliter of air */
    @RequiredHardware(Hardware.PMSX003_A) val p_2_5_um: Double? = null,
    /** PM2.5 readings from channel A using the ATM estimation of density */
    @RequiredHardware(Hardware.PMSX003_A) val pm2_5_atm: Double? = null,
    /** Channel A 5.0-micrometer and larger particle counts per deciliter of air */
    @RequiredHardware(Hardware.PMSX003_A) val p_5_0_um: Double? = null,
    /** PM10 readings from channel A using the ATM estimation of density */
    @RequiredHardware(Hardware.PMSX003_A) val pm10_0_atm: Double? = null,
    /** Channel A 10.0-micrometer particle counts per deciliter of air */
    @RequiredHardware(Hardware.PMSX003_A) val p_10_0_um: Double? = null,

    ///// PMSX003_B

    /** US EPA PM2.5 AQI as measured by channel B */
    @SerialName("pm2.5_aqi_b")
    @RequiredHardware(Hardware.PMSX003_B)
    val pm25_aqi_b: Int? = null,
    /** PM1 readings from channel B using the CF=1 estimation of density */
    @RequiredHardware(Hardware.PMSX003_B) val pm1_0_cf_1_b: Double? = null,
    /** Channel B 0.3-micrometer and larger particle counts per deciliter of air */
    @RequiredHardware(Hardware.PMSX003_B) val p_0_3_um_b: Double? = null,
    /** PM2.5 readings from channel B using the CF=1 estimation of density */
    @RequiredHardware(Hardware.PMSX003_B) val pm2_5_cf_1_b: Double? = null,
    /** Channel B 0.5-micrometer and larger particle counts per deciliter of air */
    @RequiredHardware(Hardware.PMSX003_B) val p_0_5_um_b: Double? = null,
    /** PM10 readings from channel B using the CF=1 estimation of density */
    @RequiredHardware(Hardware.PMSX003_B) val pm10_0_cf_1_b: Double? = null,
    /** Channel B 1.0-micrometer and larger particle counts per deciliter of air */
    @RequiredHardware(Hardware.PMSX003_B) val p_1_0_um_b: Double? = null,
    /** PM1 readings from channel B using the ATM estimation of density */
    @RequiredHardware(Hardware.PMSX003_B) val pm1_0_atm_b: Double? = null,
    /** Channel B 2.5-micrometer and larger particle counts per deciliter of air */
    @RequiredHardware(Hardware.PMSX003_B) val p_2_5_um_b: Double? = null,
    /** PM2.5 readings from channel B using the ATM estimation of density */
    @RequiredHardware(Hardware.PMSX003_B) val pm2_5_atm_b: Double? = null,
    /** Channel B 5.0-micrometer and larger particle counts per deciliter of air */
    @RequiredHardware(Hardware.PMSX003_B) val p_5_0_um_b: Double? = null,
    /** PM10 readings from channel B using the ATM estimation of density */
    @RequiredHardware(Hardware.PMSX003_B) val pm10_0_atm_b: Double? = null,
    /** Channel B 10.0-micrometer particle counts per deciliter of air */
    @RequiredHardware(Hardware.PMSX003_B) val p_10_0_um_b: Double? = null,
)