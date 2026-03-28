package fyi.hellochristine.purpleairtomqtt

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.google.inject.Guice
import fyi.hellochristine.purpleairtomqtt.app.AppModule
import fyi.hellochristine.purpleairtomqtt.app.Lifecycle
import fyi.hellochristine.purpleairtomqtt.app.Logging
import java.io.File


data class CLIOptions(
    val configFile: File,
)

class PurpleAirToMqtt: CliktCommand() {
    init {
        context { autoEnvvarPrefix = "PURPLEAIRTOMQTT" }
    }

    val configFile by
        option("--config-file",
            envvar = "CONFIG_FIlE",
            help = "Path to config file",
        ).file(
            mustExist = true,
            mustBeReadable = true,
            canBeDir = false,
        ).defaultLazy { File("/app/config.toml") }

    override fun run() {
        Logging.installRXLoggingHook()

        val cliOptions = CLIOptions(configFile = configFile)
        val injector = Guice.createInjector(AppModule(cliOptions))
        val poller = injector.getInstance(Poller::class.java)
        val lifecycle = injector.getInstance(Lifecycle::class.java)
        poller.start()
        lifecycle.waitForShutdown()
    }
}

fun main(args: Array<String>) = PurpleAirToMqtt().main(args)
