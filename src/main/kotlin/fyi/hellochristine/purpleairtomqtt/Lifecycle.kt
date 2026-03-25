package fyi.hellochristine.purpleairtomqtt

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import io.github.oshai.kotlinlogging.KotlinLogging
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
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

class LifecycleModule : AbstractModule() {
    private val logger = KotlinLogging.logger { }

    @Provides
    @Singleton
    fun provideShutdownHook(
        scheduler: Scheduler,
    ): Lifecycle {
        val completable = Completable.create { emitter ->
            Runtime.getRuntime().addShutdownHook(Thread {
                logger.trace { "SIGTERM received, shutting down" }
                emitter.onComplete()
            })
        }.cache()

        completable.subscribe {
            logger.trace { "Shutting down Rx Schedulers" }
            Schedulers.shutdown()
        }

        return Lifecycle(completable)
    }

    @Provides
    @Singleton
    fun provideScheduler(): Scheduler {
        return Schedulers.io()
    }
}
