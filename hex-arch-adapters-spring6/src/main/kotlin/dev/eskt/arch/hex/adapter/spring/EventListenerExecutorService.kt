package dev.eskt.arch.hex.adapter.spring

import dev.eskt.arch.hex.adapter.common.Bookmark
import dev.eskt.arch.hex.adapter.common.singleStreamTypeEventFlow
import dev.eskt.arch.hex.port.SingleStreamTypeEventListener
import dev.eskt.store.api.EventStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.seconds

@Component
public class EventListenerExecutorService(
    private val eventStore: EventStore,
    private val bookmark: Bookmark,
    private val transactionTemplate: TransactionTemplate,
    private val eventListeners: List<SingleStreamTypeEventListener<*, *>>,
    @Autowired(required = false)
    private val interceptor: EventListenerWorkerThreadInterceptor?,
) : InitializingBean, DisposableBean {
    private val logger: Logger = LoggerFactory.getLogger(EventListenerExecutorService::class.java)

    private val batchSize = 100
    private val backoff = 15.seconds

    private val dispatcher = run {
        val threadNumberCounter = AtomicInteger()
        Executors.newScheduledThreadPool(4) { runnable ->
            val interceptedRunnable: () -> Unit = {
                if (interceptor != null) interceptor.intercept { runnable.run() }
                else runnable.run()
            }
            val nextThreadNumber = threadNumberCounter.incrementAndGet()
            Thread(interceptedRunnable, "evt-listener-$nextThreadNumber").apply {
                isDaemon = true
            }
        }.asCoroutineDispatcher()
    }
    private val supervisor = SupervisorJob()
    private val scope = CoroutineScope(dispatcher + supervisor)

    private var stopped = false

    private fun init() {

        // TODO enforce uniqueness of the ids of the listeners

        logger.info("Starting listener processes for ${eventListeners.size} listeners...")
        eventListeners.forEach { genericListener ->
            @Suppress("UNCHECKED_CAST")
            val eventListener = genericListener as SingleStreamTypeEventListener<Any, Any>
            scope.launch {
                while (!stopped) {
                    logger.info("Starting collection of events for $eventListener")
                    try {
                        eventStore
                            .singleStreamTypeEventFlow(
                                streamType = eventListener.streamType,
                                sincePosition = bookmark.get(eventListener.id),
                                batchSize = batchSize,
                            )
                            .collect { envelope ->
                                transactionTemplate.execute {
                                    eventListener.listen(envelope)
                                    bookmark.set(eventListener.id, envelope.position)
                                }
                                logger.info("Processed event position ${envelope.position} of type ${envelope.event::class.qualifiedName} in $eventListener")
                            }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        logger.error("Error while collecting events in $eventListener, will try to restart in $backoff", e)
                        if (!stopped) delay(backoff)
                    }
                }
            }
        }
    }

    private fun shutdown() {
        logger.info("Shutting down...")
        stopped = true
        supervisor.cancel("Shutting down")
        dispatcher.close()
    }

    override fun afterPropertiesSet() {
        init()
    }

    override fun destroy() {
        shutdown()
    }
}
