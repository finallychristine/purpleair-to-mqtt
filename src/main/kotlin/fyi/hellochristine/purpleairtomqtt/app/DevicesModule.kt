package fyi.hellochristine.purpleairtomqtt.app

import dagger.Module
import dagger.Provides
import fyi.hellochristine.purpleairtomqtt.config.Config
import fyi.hellochristine.purpleairtomqtt.config.DeviceConfig
import fyi.hellochristine.purpleairtomqtt.model.Device
import java.time.Duration
import javax.inject.Singleton

@Module
class DevicesModule {
    @Provides
    @Singleton
    fun provideDevices(config: Config): List<Device> {
        return config.devices.map { configDeviceToDevice(config, it.key, it.value)  }
    }

    private fun configDeviceToDevice(config: Config, id: String, cfg: DeviceConfig): Device {
        cfg.servers.forEach { server ->
            checkNotNull(config.mqtt[server]) { "Device '$id' references MQTT server '$server' that is not configured" }
        }

        return Device(
            id = id,
            host = cfg.host,
            brokerIds = cfg.servers.toSet(),
            pollRate = Duration.ofSeconds(cfg.pollRateSeconds),
        )
    }
}
