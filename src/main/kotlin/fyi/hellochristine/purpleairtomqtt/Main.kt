package fyi.hellochristine.purpleairtomqtt

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import fyi.hellochristine.purpleairtomqtt.app.AppComponent
import fyi.hellochristine.purpleairtomqtt.app.DaggerAppComponent
import fyi.hellochristine.purpleairtomqtt.app.Logging
import org.jetbrains.annotations.VisibleForTesting
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

    companion object {
        @VisibleForTesting
        fun setup(cliOptions: CLIOptions): AppComponent {
            Logging.installRXLoggingHook()
            return DaggerAppComponent.factory().create(cliOptions = cliOptions)
        }
    }

    override fun run() {
        val cliOptions = CLIOptions(configFile = configFile)
        val appComponent = setup(cliOptions)
        appComponent.getPoller().start()
        val lifecycle = appComponent.getLifecycle()
        lifecycle.waitForShutdown()
    }
}

fun main(args: Array<String>) = PurpleAirToMqtt().main(args)
