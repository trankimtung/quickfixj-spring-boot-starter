package com.trankimtung.quickfixj.spring.boot.starter.connection

import com.trankimtung.quickfixj.spring.boot.starter.exception.SessionSettingsException
import org.slf4j.LoggerFactory
import org.springframework.context.SmartLifecycle
import quickfix.ConfigError
import quickfix.Connector
import quickfix.RuntimeError
import java.lang.IllegalStateException


/**
 * [SmartLifecycle] manager for [Connector], responsible for starting and stopping the [Connector]
 * when application context is refreshed and/or shutdown.
 *
 * @param connector the underlying [Connector].
 * @param autoStartup should the connector starts when application context has been refreshed.
 * @param startupPhase start/shutdown phase for this [SmartLifecycle]] manager.
 * @param forceDisconnect should the [Connector] wait for logout completion before stopping.
 */
class ConnectorManager(
    private val connector: Connector,
    private val autoStartup: Boolean = true,
    private val startupPhase: Int = Int.MAX_VALUE,
    @Suppress("MemberVisibilityCanBePrivate") val forceDisconnect: Boolean = false,
) : SmartLifecycle {

    companion object {
        private val log = LoggerFactory.getLogger(ConnectorManager::class.java)
    }

    private var running = false

    /**
     * Starts the connector and starts accepting connections.
     */
    @Synchronized
    override fun start() {
        if (!isRunning) {
            log.info("Starts QuickFix/J Connector.")
            try {
                connector.start()
            } catch (e: Throwable) {
                when (e) {
                    is ConfigError, is RuntimeError -> throw SessionSettingsException(e.message, e)
                    else -> throw IllegalStateException("Failed to start ConnectorManager", e)
                }
            }
            running = true
        }
    }

    /**
     * Stops all sessions, optionally waiting for logout completion, and shuts the connector down.
     */
    @Synchronized
    override fun stop() {
        if (isRunning) {
            log.info("Stops QuickFix/J Connector, force disconnect = $forceDisconnect")
            try {
                connector.stop(forceDisconnect)
            } finally {
                running = false
            }
        }
    }

    /**
     * Stops all sessions, optionally waiting for logout completion, and shuts the connector down.
     *
     * @param callback callback to execute after the connector has been shut down.
     */
    @Synchronized
    override fun stop(callback: Runnable) {
        stop()
        callback.run()
    }

    @Synchronized
    override fun isRunning(): Boolean =
        running

    override fun isAutoStartup(): Boolean =
        autoStartup

    override fun getPhase(): Int =
        startupPhase
}