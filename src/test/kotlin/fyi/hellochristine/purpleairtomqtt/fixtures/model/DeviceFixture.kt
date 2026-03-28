package fyi.hellochristine.purpleairtomqtt.fixtures.model

import fyi.hellochristine.purpleairtomqtt.model.Device
import java.time.Duration

object DeviceFixture {
    const val id = "device-id"
    const val host = "device-host.example"
    const val brokerId = "broker-id"
    val pollRate = Duration.ofSeconds(60)

    fun createDevice(
        id: String = this.id,
        host: String = this.host,
        brokerIds: Set<String> = setOf(this.brokerId),
        pollRate: Duration = this.pollRate,
    ): Device {
        return Device(
            id = id,
            host = host,
            brokerIds = brokerIds,
            pollRate = pollRate,
        )
    }
}
