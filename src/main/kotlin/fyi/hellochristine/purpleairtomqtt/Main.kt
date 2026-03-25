package fyi.hellochristine.purpleairtomqtt

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.google.inject.AbstractModule
import com.google.inject.Guice
import java.io.File

class AppModule(
    private val cliOptions: CLIOptions
): AbstractModule() {
    override fun configure() {
        bind(CLIOptions::class.java).toInstance(cliOptions)
        install(LifecycleModule())
        install(DevicesModule())
        install(ConfigModule())
        install(MqttModule())
        install(DeviceHttpClientModule())
    }
}

data class CLIOptions(
    val configFile: File,
)

class PurpleAirToMqtt: CliktCommand() {
    init {
        context { autoEnvvarPrefix = "PURPLEAIRTOMQTT" }
    }

    val configFile by option("--config-file", envvar = "CONFIG_FIle", help = "Path to config file").file().required()

    override fun run() {
        installRXLoggingHook()

        val cliOptions = CLIOptions(configFile = configFile)
        val injector = Guice.createInjector(AppModule(cliOptions))
        val poller = injector.getInstance(Poller::class.java)
        val lifecycle = injector.getInstance(Lifecycle::class.java)
        poller.start()
        lifecycle.waitForShutdown()
    }
}

fun main(args: Array<String>) = PurpleAirToMqtt().main(args)
