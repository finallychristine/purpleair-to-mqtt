package fyi.hellochristine.purpleairtomqtt.app

import dagger.BindsInstance
import dagger.Component
import fyi.hellochristine.purpleairtomqtt.CLIOptions
import fyi.hellochristine.purpleairtomqtt.Poller
import fyi.hellochristine.purpleairtomqtt.config.ConfigModule
import fyi.hellochristine.purpleairtomqtt.mqtt.MqttModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    DevicesModule::class,
    LifecycleModule::class,
    ConfigModule::class,
    MqttModule::class,
    OkHttpClientModule::class,
])
interface AppComponent {
    fun getPoller(): Poller
    fun getLifecycle(): Lifecycle

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance cliOptions: CLIOptions,
        ): AppComponent
    }
}
