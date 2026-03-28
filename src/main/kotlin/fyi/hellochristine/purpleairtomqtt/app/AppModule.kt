package fyi.hellochristine.purpleairtomqtt.app

import com.google.inject.AbstractModule
import fyi.hellochristine.purpleairtomqtt.CLIOptions
import fyi.hellochristine.purpleairtomqtt.config.ConfigModule
import fyi.hellochristine.purpleairtomqtt.mqtt.MqttModule

class AppModule(
    private val cliOptions: CLIOptions
): AbstractModule() {
    override fun configure() {
        bind(CLIOptions::class.java).toInstance(cliOptions)
        install(LifecycleModule())
        install(DevicesModule())
        install(ConfigModule())
        install(MqttModule())
        install(OkHttpClientModule())
    }
}
