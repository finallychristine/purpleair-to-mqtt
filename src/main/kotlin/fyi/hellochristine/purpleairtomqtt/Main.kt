package fyi.hellochristine.purpleairtomqtt

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Provides
import com.google.inject.Singleton
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

class AppModule: AbstractModule() {
    override fun configure() {
        install(LifecycleModule())
        install(DevicesModule())
        install(ConfigModule())
        install(MqttModule())
        install(DeviceHttpClientModule())
    }

    @Provides
    @Singleton
    fun provideLogger(): KLogger {
        return KotlinLogging.logger("purpleairtomqtt")
    }
}

fun main() {
    val injector = Guice.createInjector(AppModule())
    val poller = injector.getInstance(Poller::class.java)
    val lifecycle = injector.getInstance(Lifecycle::class.java)
    poller.start()
    lifecycle.waitForShutdown()
}