package fyi.hellochristine.purpleairtomqtt.app

import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import io.github.oshai.kotlinlogging.KotlinLogging
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers

@Module
class LifecycleModule {
    private val logger = KotlinLogging.logger { }

    @Provides
    @Singleton
    fun provideShutdownHook(): Lifecycle {
        val lifecycle = Lifecycle()

        Runtime.getRuntime().addShutdownHook(Thread {
            logger.trace { "SIGTERM received, shutting down" }
            lifecycle.shutdown()
        })

        lifecycle.onShutdown.subscribe {
            logger.trace { "Shutting down Rx Schedulers" }
            Schedulers.shutdown()
        }

        return lifecycle
    }
}
