package fyi.hellochristine.purpleairtomqtt

import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.Provides
import com.google.inject.Singleton
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.SingleSubject

class Lifecycle(
    val onShutdown: Single<Boolean>,
) {
    private var shutdown = false

    init {
        onShutdown.subscribe { shutdown = true }
    }

    fun waitForShutdown() {
        onShutdown.blockingGet()
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
        val subject = SingleSubject.create<Boolean>()
        Runtime.getRuntime().addShutdownHook(Thread {
            subject.onSuccess(true)
        })
        val single = subject.subscribeOn(Schedulers.io())

        single.subscribe {
            logger.info { "Shutdown signal received" }
            scheduler.shutdown()
        }

        return Lifecycle(single)
    }

    @Provides
    @Singleton
    fun provideScheduler(): Scheduler {
        return Schedulers.io()
    }
}
