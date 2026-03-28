package fyi.hellochristine.purpleairtomqtt.model

import java.time.Duration

/**
 * A Device represents a PurpleAir device configured in a configuration file. We eventually
 * poll the PurpleAir API, and determine the available [Sensor]s for this device.
 */
data class Device(
    /** Device ID specified by the user in the configuration file */
    val id: String,
    val host: String,
    val brokerIds: Set<String>,
    val pollRate: Duration,
) {
    fun describe() = id
}
