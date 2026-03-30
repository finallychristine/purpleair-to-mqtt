package fyi.hellochristine.purpleairtomqtt.fixtures.homeassistant

import fyi.hellochristine.purpleairtomqtt.fixtures.model.DeviceFixture
import fyi.hellochristine.purpleairtomqtt.fixtures.model.SensorFixture
import fyi.hellochristine.purpleairtomqtt.homeassistant.Device
import fyi.hellochristine.purpleairtomqtt.homeassistant.DeviceClass
import fyi.hellochristine.purpleairtomqtt.homeassistant.Sensor
import fyi.hellochristine.purpleairtomqtt.homeassistant.SensorWithValue
import fyi.hellochristine.purpleairtomqtt.homeassistant.StateClass
import fyi.hellochristine.purpleairtomqtt.homeassistant.UnitOfMeasurement

object HomeAssistantFixture {
    const val sensorName = "Some Sensor"
    const val sensorId = "some-sensor"
    const val sensorValue = 123.4

    fun createSensor(
        deviceId: String = DeviceFixture.id,
        device: Device = createDevice(),
        sensorId: String = this.sensorId,
        name: String = sensorName,
        deviceClass: DeviceClass? = DeviceClass.TEMPERATURE,
        unitOfMeasurement: UnitOfMeasurement? = UnitOfMeasurement.CELSIUS,
        enabledByDefault: Boolean = true,
    ): Sensor {
        return Sensor(
            name = name,
            deviceClass = deviceClass,
            unitOfMeasurement = unitOfMeasurement,
            stateTopic = "purpleairtomqtt/${deviceId}/${name}/state",
            availabilityTopic = "purpleairtomqtt/${deviceId}/${name}/status",
            uniqueId = sensorId,
            enabledByDefault = enabledByDefault,
            device = device,
            stateClass = StateClass.MEASUREMENT,
        )
    }


    fun createDevice(): Device {
        return Device(
            ids = listOf(
                "purpleair-to-mqtt--${DeviceFixture.id}",
                SensorFixture.deviceId,
            ),
            name = SensorFixture.geo,
            model = SensorFixture.hardwareDiscovered,
            configurationUrl = "http://${DeviceFixture.host}",
            softwareVersion = SensorFixture.softwareVersion,
            connections = listOf(listOf("mac", SensorFixture.deviceId)),
        )
    }

    fun createSensorWithValue(
        sensorId: String = this.sensorId,
        deviceId: String = DeviceFixture.id,
        sensor: Sensor = createSensor(sensorId = sensorId),
        haDiscoveryTopic: String = "homeassistant/sensor/purpleairtomqtt-${deviceId}--${sensorId}/config",
        value: Any = sensorValue
    ): SensorWithValue {
        return SensorWithValue(
            value = value,
            sensor = sensor,
            haDiscoveryTopic = haDiscoveryTopic,
        )
    }
}
