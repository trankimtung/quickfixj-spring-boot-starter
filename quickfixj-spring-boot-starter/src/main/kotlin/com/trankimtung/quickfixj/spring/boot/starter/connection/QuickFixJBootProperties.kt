package com.trankimtung.quickfixj.spring.boot.starter.connection

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import quickfix.Connector

/**
 * QuickFix/J starter properties.
 */
@ConstructorBinding
@ConfigurationProperties(prefix = QuickFixJBootProperties.PROPERTY_PREFIX)
data class QuickFixJBootProperties(

    /**
     * Client properties.
     */
    val client: ConnectorConfig = ConnectorConfig(),

    /**
     * Server properties.
     */
    val server: ConnectorConfig = ConnectorConfig()
) {
    companion object {
        const val PROPERTY_PREFIX = "kt.quickfixj"
    }

    /**
     * QuickFix/J connector properties.
     */
    data class ConnectorConfig(

        /**
         * QuickFix/J configuration file location.
         */
        var config: String? = null,

        /**
         * Should the [quickfix.Connector] starts when application context has been refreshed.
         */
        var autoStartup: Boolean = true,

        /**
         * Phase in which the [Connector] should start/shutdown.
         *
         * @see [org.springframework.context.SmartLifecycle]
         */
        var startupPhase: Int = Int.MAX_VALUE,

        /**
         * Should the [quickfix.Connector] wait for logout to complete before stopping.
         */
        var forceDisconnect: Boolean = false,

        /**
         * JMX configuration.
         */
        var jmx: Jmx = Jmx(),

        /**
         * Threading model configuration.
         */
        var concurrent: Concurrent = Concurrent(),

        /**
         * Message store factory configuration.
         */
        var messageStoreFactory: MessageStoreFactory = MessageStoreFactory(),

        /**
         * Log factory configuration.
         */
        var logFactory: LogFactory = LogFactory()
    ) {

        /**
         * Threading model configuration
         */
        data class Concurrent(

            /**
             * Decides if the [Connector] should use multi-thread to process messages.
             *
             * - if *true*, accepts connections and uses a separate thread per session to process messages.
             * - if *false*, accepts connections and uses a single thread to process messages for all sessions.
             */
            var enabled: Boolean = false
        )

        /**
         * Jmx configuration.
         */
        data class Jmx(

            /**
             * Enables JMX support
             */
            var enabled: Boolean = false
        )

        /**
         * [MessageStoreFactory] configuration.
         */
        data class MessageStoreFactory(

            /**
             * The [MessageStoreFactoryType] to use.
             */
            var type: MessageStoreFactoryType = MessageStoreFactoryType.MEMORY,

            /**
             * Uses Spring's DataSource when type is [MessageStoreFactoryType.JDBC].
             */
            var useSpringJdbc: Boolean = true
        )

        /**
         * [LogFactory] configuration.
         */
        data class LogFactory(

            /**
             * The [MessageStoreFactoryType] list to use.
             */
            var type: Array<LogFactoryType> = arrayOf(LogFactoryType.SLF4J),

            /**
             * Uses Spring's DataSource when type is [LogFactoryType.JDBC].
             */
            var useSpringJdbc: Boolean = true
        ) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as LogFactory

                if (!type.contentEquals(other.type)) return false
                if (useSpringJdbc != other.useSpringJdbc) return false

                return true
            }

            override fun hashCode(): Int {
                var result = type.contentHashCode()
                result = 31 * result + useSpringJdbc.hashCode()
                return result
            }
        }
    }
}