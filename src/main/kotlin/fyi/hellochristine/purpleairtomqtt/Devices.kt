package fyi.hellochristine.purpleairtomqtt

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import java.time.Duration

data class Device(
    val id: String,
    val host: String,
    val servers: Set<String>,
    val pollRate: Duration,
) {
    fun describe() = id
}

class DevicesModule: AbstractModule() {
    @Provides
    @Singleton
    fun provideDevices(config: Config): List<Device> {
        return config.devices.map { configDeviceToDevice(it.key, it.value)  }
    }

    private fun configDeviceToDevice(id: String, cfg: DeviceConfig): Device {
        return Device(
            id = id,
            host = cfg.host,
            servers = cfg.servers.toSet(),
            pollRate = Duration.ofSeconds(cfg.pollRateSeconds),
        )
    }
}