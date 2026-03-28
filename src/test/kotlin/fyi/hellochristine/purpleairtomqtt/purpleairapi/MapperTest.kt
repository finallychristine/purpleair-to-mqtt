package fyi.hellochristine.purpleairtomqtt.purpleairapi

import fyi.hellochristine.purpleairtomqtt.fixtures.model.DeviceFixture
import fyi.hellochristine.purpleairtomqtt.fixtures.model.SensorFixture
import fyi.hellochristine.purpleairtomqtt.fixtures.purpleairapi.DeviceResponseFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class MapperTest {

    @Test
    fun apiResponseToSensor() {
        val device = DeviceFixture.createDevice()
        val response = DeviceResponseFixture.createDeviceResponse()

        val expected = SensorFixture.createSensor(device)

        val actual = Mapper.apiResponseToSensor(device, response)

        assertThat(actual)
            .usingRecursiveComparison()
            .withStrictTypeChecking()
            .isEqualTo(expected)
    }
}
