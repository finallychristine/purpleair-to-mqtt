package fyi.hellochristine.purpleairtomqtt.app

import io.reactivex.rxjava3.core.Completable
import kotlin.system.exitProcess

class Lifecycle(
    val onShutdown: Completable,
) {
    private var shutdown = false

    init {
        onShutdown.subscribe { shutdown = true }
    }

    fun waitForShutdown() {
        onShutdown.blockingAwait()
        exitProcess(0)
    }

    fun isShutDown(): Boolean {
        return shutdown
    }
}

