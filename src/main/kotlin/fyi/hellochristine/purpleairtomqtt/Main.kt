package fyi.hellochristine.purpleairtomqtt

import com.google.inject.AbstractModule
import com.google.inject.Guice

class AppModule: AbstractModule() {
    override fun configure() {
        install(LifecycleModule())
        install(DevicesModule())
        install(ConfigModule())
        install(MqttModule())
        install(DeviceHttpClientModule())
    }
}

fun main() {
    val injector = Guice.createInjector(AppModule())
    val poller = injector.getInstance(Poller::class.java)
    val lifecycle = injector.getInstance(Lifecycle::class.java)
    poller.start()
    lifecycle.waitForShutdown()
}
