package fyi.hellochristine.purpleairtomqtt.app

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.subjects.CompletableSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlin.system.exitProcess

class Lifecycle {
    private var shutdown = false

    private var onShutdownSubject = CompletableSubject.create()
    val onShutdown: Completable = onShutdownSubject

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

    fun shutdown() {
        onShutdownSubject.onComplete()
    }
}

