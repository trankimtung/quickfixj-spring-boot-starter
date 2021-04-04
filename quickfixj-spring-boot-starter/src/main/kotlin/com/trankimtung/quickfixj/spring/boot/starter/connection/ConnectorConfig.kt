package com.trankimtung.quickfixj.spring.boot.starter.connection

import quickfix.Connector

/**
 * QuickFix/J [Connector] configuration.
 */
@Suppress("unused")
class ConnectorConfig {

    /**
     * QuickFix/J configuration file location.
     */
    var config: String? = null

    /**
     * Should the [quickfix.Connector] starts when application context has been refreshed.
     */
    var autoStartup: Boolean = true

    /**
     * Phase which the [Connector] should start/shutdown.
     *
     * @see [org.springframework.context.SmartLifecycle]
     */
    var startupPhase: Int = Int.MAX_VALUE

    /**
     * Should the [quickfix.Connector] wait for logout completion before stopping.
     */
    var forceDisconnect: Boolean = false

    /**
     * JMX configuration.
     */
    var jmx = Jmx()

    /**
     * Threading model configuration.
     */
    var concurrent = Concurrent()

    /**
     * Message store factory configuration.
     */
    var messageStoreFactory = MessageStoreFactory()

    /**
     * Log factory configuration.
     */
    var logFactory = LogFactory()

    class Concurrent {

        /**
         * Decides if the [Connector] should use multi-thread to process messages.
         *
         * - if *true*, accepts connections and uses a separate thread per session to process messages.
         * - if *false*, accepts connections and uses a single thread to process messages for all sessions.
         */
        var enabled = false
    }

    /**
     * Jmx configuration.
     */
    class Jmx {

        /**
         * Enables JMX support
         */
        var enabled = false
    }

    /**
     * [MessageStoreFactory] configuration.
     */
    class MessageStoreFactory {

        /**
         * The [MessageStoreFactoryType] to use.
         */
        var type: MessageStoreFactoryType = MessageStoreFactoryType.MEMORY

        /**
         * Uses Spring's DataSource when type is [MessageStoreFactoryType.JDBC].
         */
        var useSpringJdbc: Boolean = true
    }

    /**
     * [LogFactory] configuration.
     */
    class LogFactory {

        /**
         * The [MessageStoreFactoryType] list to use.
         */
        var type: Array<LogFactoryType> = arrayOf(LogFactoryType.SLF4J)

        /**
         * Uses Spring's DataSource when type is [LogFactoryType.JDBC].
         */
        var useSpringJdbc: Boolean = true
    }
}