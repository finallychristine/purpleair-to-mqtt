package fyi.hellochristine.purpleairtomqtt

import io.reactivex.rxjava3.plugins.RxJavaPlugins
import org.slf4j.MDC

fun installRXLoggingHook() {
    RxJavaPlugins.setScheduleHandler { runnable ->
        val ctx: Map<String,String>? = MDC.getCopyOfContextMap()
        val hook: Runnable = {
            if (ctx != null) {
                MDC.setContextMap(ctx)
            }

            try {
                runnable.run()
            } finally {
                MDC.clear()
            }
        }
        hook
    }
}
