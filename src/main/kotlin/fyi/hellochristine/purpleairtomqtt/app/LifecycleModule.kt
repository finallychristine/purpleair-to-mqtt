package fyi.hellochristine.purpleairtomqtt.app

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import io.github.oshai.kotlinlogging.KotlinLogging
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers

class LifecycleModule : AbstractModule() {
    private val logger = KotlinLogging.logger { }

    @Provides
    @Singleton
    fun provideShutdownHook(): Lifecycle {
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
}
