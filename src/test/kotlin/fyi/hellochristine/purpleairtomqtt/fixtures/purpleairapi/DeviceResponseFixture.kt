package fyi.hellochristine.purpleairtomqtt.fixtures.purpleairapi

import fyi.hellochristine.purpleairtomqtt.fixtures.model.SensorFixture
import fyi.hellochristine.purpleairtomqtt.purpleairapi.DeviceResponse

object DeviceResponseFixture {
    const val place = "inside"

    fun createDeviceResponse(): DeviceResponse {
        return DeviceResponse(
            sensorId = SensorFixture.deviceId,
            geo = SensorFixture.geo,
            place = place,
            hardwareDiscovered = SensorFixture.hardwareDiscovered,
            firmwareVersion = SensorFixture.softwareVersion,

            tempF = SensorFixture.temperatureF,
            humidity = SensorFixture.humidity,
            dewpointF = SensorFixture.dewpointF,
            pressure = SensorFixture.pressure,

            // a-channel
            // Note: for convention, PM size + 0.01 for ATM, PM size + 0.02 for CF1
            pm25_aqi = SensorFixture.aqi,
            pm1_0_atm = 1.01,
            pm2_5_atm = 2.51,
            pm10_0_atm = 10.01,
            pm1_0_cf_1 = 1.02,
            pm2_5_cf_1 = 2.52,
            pm10_0_cf_1 = 10.02,

            // particle couts use raw diameter
            p_0_3_um = 0.3,
            p_0_5_um = 0.5,
            p_1_0_um = 1.0,
            p_2_5_um = 2.5,
            p_5_0_um = 5.0,
            p_10_0_um = 10.0,

            // b-channel unused for now
        )
    }
}
