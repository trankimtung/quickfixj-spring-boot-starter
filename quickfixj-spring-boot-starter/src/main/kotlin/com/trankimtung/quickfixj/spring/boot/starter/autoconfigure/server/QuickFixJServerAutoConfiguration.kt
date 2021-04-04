package com.trankimtung.quickfixj.spring.boot.starter.autoconfigure.server

import com.trankimtung.quickfixj.spring.boot.starter.connection.*
import com.trankimtung.quickfixj.spring.boot.starter.connection.LogFactoryType.*
import com.trankimtung.quickfixj.spring.boot.starter.exception.SessionSettingsException
import org.quickfixj.jmx.JmxExporter
import org.springframework.boot.autoconfigure.condition.*
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import quickfix.*
import javax.management.ObjectName
import javax.sql.DataSource

/**
 * Auto configuration class to setup a QuickFix/J acceptor.
 */
@Configuration
@EnableConfigurationProperties(QuickFixJBootProperties::class)
@ConditionalOnBean(QuickFixJServerMarkerConfiguration.Marker::class)
class QuickFixJServerAutoConfiguration {

    companion object {
        const val QUICKFIXJ_SERVER_CONFIG_FILENAME = "quickfixj-server.cfg"
        const val PROPERTY_PREFIX = "${QuickFixJBootProperties.PROPERTY_PREFIX}.server"
        private const val PROPERTY_MESSAGE_STORE_FACTORY_TYPE = "message-store-factory.type"
    }

    /**
     * Loads the [Connector] session settings from config file.
     * Search order:
     * - Location defined in application properties.
     * - quickfixj-server.cfg in working directory.
     * - quickfixj-server.cfg in classpath.
     */
    @Bean
    @ConditionalOnMissingBean(SessionSettings::class)
    fun quickFixJServerSessionSettings(properties: QuickFixJBootProperties): SessionSettings {
        return SessionSettingsLoader.loadSettings(
            properties.server.config,
            "file:./${QUICKFIXJ_SERVER_CONFIG_FILENAME}",
            "classpath:/${QUICKFIXJ_SERVER_CONFIG_FILENAME}"
        )
    }

    /**
     * Creates a [MessageFactory].
     */
    @Bean
    @ConditionalOnMissingBean(MessageFactory::class)
    fun quickFixJServerMessageFactory(): MessageFactory {
        return DefaultMessageFactory()
    }

    /**
     * Creates a [ConnectorCallbackDistributor] that forwards events to other [ConnectorCallback] beans.
     */
    @Bean
    @ConditionalOnMissingBean(name = ["quickFixJServerApplication"])
    fun quickFixJServerApplication(
        @Suppress("SpringJavaInjectionPointsAutowiringInspection") connectorCallbacks: List<ConnectorCallback>
    ): Application {
        return ConnectorCallbackDistributor(connectorCallbacks)
    }

    /**
     * Creates a single-threaded [Acceptor].
     *
     * @param serverApplication          [Application]
     * @param serverMessageStoreFactory  [MessageStoreFactory]
     * @param serverSessionSettings      [SessionSettings]
     * @param serverLogFactory           [LogFactory]
     * @param serverMessageFactory       [MessageFactory]
     * @return [Acceptor]
     * @throws ConfigError when a configuration error is detected.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = "${PROPERTY_PREFIX}.concurrent",
        name = ["enabled"],
        havingValue = "false",
        matchIfMissing = true
    )
    @Throws(ConfigError::class)
    fun quickFixJServerSocketAcceptor(
        serverApplication: Application,
        @Suppress("SpringJavaInjectionPointsAutowiringInspection") serverMessageStoreFactory: MessageStoreFactory,
        serverSessionSettings: SessionSettings,
        serverLogFactory: LogFactory,
        serverMessageFactory: MessageFactory
    ): Acceptor {
        return SocketAcceptor.newBuilder()
            .withApplication(serverApplication)
            .withMessageStoreFactory(serverMessageStoreFactory)
            .withSettings(serverSessionSettings)
            .withLogFactory(serverLogFactory)
            .withMessageFactory(serverMessageFactory)
            .build()
    }

    /**
     * Creates a multi-threaded [Acceptor].
     *
     * @param serverApplication         [Application]
     * @param serverMessageStoreFactory [MessageStoreFactory]
     * @param serverSessionSettings     [SessionSettings]
     * @param serverLogFactory          [LogFactory]
     * @param serverMessageFactory      [MessageFactory]
     * @return [Acceptor]
     * @throws ConfigError when a configuration error is detected.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = "${PROPERTY_PREFIX}.concurrent",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = false
    )
    @Throws(ConfigError::class)
    fun quickFixJServerThreadedSocketAcceptor(
        serverApplication: Application,
        @Suppress("SpringJavaInjectionPointsAutowiringInspection") serverMessageStoreFactory: MessageStoreFactory,
        serverSessionSettings: SessionSettings,
        serverLogFactory: LogFactory,
        serverMessageFactory: MessageFactory
    ): Acceptor {
        return ThreadedSocketAcceptor.newBuilder()
            .withApplication(serverApplication)
            .withMessageStoreFactory(serverMessageStoreFactory)
            .withSettings(serverSessionSettings)
            .withLogFactory(serverLogFactory)
            .withMessageFactory(serverMessageFactory)
            .build()
    }

    /**
     * Setups [ConnectorManager]
     */
    @Bean
    fun quickFixJServerConnectorManager(
        @Suppress("SpringJavaInjectionPointsAutowiringInspection") acceptor: Acceptor,
        properties: QuickFixJBootProperties
    ): ConnectorManager {
        return with(properties.server) {
            ConnectorManager(acceptor, autoStartup, startupPhase, forceDisconnect)
        }
    }

    /**
     * Setups a [CompositeLogFactory] that wraps all factories defined in configuration.
     */
    @Bean
    @ConditionalOnMissingBean(LogFactory::class)
    fun quickFixJServerLogFactory(
        sessionSettings: SessionSettings,
        properties: QuickFixJBootProperties,
        datasource: DataSource?
    ): LogFactory {
        val logFactories = properties.server.logFactory.type.map {
            when (it) {
                SCREEN -> ScreenLogFactory(sessionSettings)
                SLF4J -> SLF4JLogFactory(sessionSettings)
                FILE -> FileLogFactory(sessionSettings)
                JDBC -> JdbcLogFactory(sessionSettings).apply {
                    if (properties.server.logFactory.useSpringJdbc) {
                        setDataSource(datasource ?: throw IllegalStateException("DataSource not set."))
                    }
                }
            }
        }
        return CompositeLogFactory(logFactories.toTypedArray())
    }

    /**
     * Message store configuration
     */
    @Configuration
    class MessageStoreConfiguration {

        @Bean
        @ConditionalOnMissingBean(MessageStoreFactory::class)
        @ConditionalOnProperty(
            prefix = PROPERTY_PREFIX,
            name = [PROPERTY_MESSAGE_STORE_FACTORY_TYPE],
            havingValue = "NOOP",
            matchIfMissing = false
        )
        fun quickFixJServerNoopMessageStoreFactory(sessionSettings: SessionSettings): MessageStoreFactory {
            return NoopStoreFactory()
        }

        @Bean
        @ConditionalOnMissingBean(MessageStoreFactory::class)
        @ConditionalOnProperty(
            prefix = PROPERTY_PREFIX,
            name = [PROPERTY_MESSAGE_STORE_FACTORY_TYPE],
            havingValue = "MEMORY",
            matchIfMissing = true
        )
        fun quickFixJServerMemoryMessageStoreFactory(sessionSettings: SessionSettings): MessageStoreFactory {
            return MemoryStoreFactory()
        }

        @Bean
        @ConditionalOnMissingBean(MessageStoreFactory::class)
        @ConditionalOnProperty(
            prefix = PROPERTY_PREFIX,
            name = [PROPERTY_MESSAGE_STORE_FACTORY_TYPE],
            havingValue = "JDBC",
            matchIfMissing = false
        )
        fun quickFixJServerJdbcMessageStoreFactory(
            sessionSettings: SessionSettings,
            properties: QuickFixJBootProperties,
            datasource: DataSource?
        ): MessageStoreFactory {
            return JdbcStoreFactory(sessionSettings).apply {
                if (properties.server.messageStoreFactory.useSpringJdbc) {
                    setDataSource(datasource ?: throw IllegalStateException("DataSource not set."))
                }
            }
        }

        @Bean
        @ConditionalOnMissingBean(MessageStoreFactory::class)
        @ConditionalOnProperty(
            prefix = PROPERTY_PREFIX,
            name = [PROPERTY_MESSAGE_STORE_FACTORY_TYPE],
            havingValue = "FILE",
            matchIfMissing = false
        )
        fun quickFixJServerFileMessageStoreFactory(sessionSettings: SessionSettings): MessageStoreFactory {
            return FileStoreFactory(sessionSettings)
        }

        @Bean
        @ConditionalOnMissingBean(MessageStoreFactory::class)
        @ConditionalOnProperty(
            prefix = PROPERTY_PREFIX,
            name = [PROPERTY_MESSAGE_STORE_FACTORY_TYPE],
            havingValue = "CACHED_FILE",
            matchIfMissing = false
        )
        fun quickFixJServerCachedFileMessageStoreFactory(sessionSettings: SessionSettings): MessageStoreFactory {
            return CachedFileStoreFactory(sessionSettings)
        }

        @Bean
        @ConditionalOnMissingBean(MessageStoreFactory::class)
        @ConditionalOnProperty(
            prefix = PROPERTY_PREFIX,
            name = [PROPERTY_MESSAGE_STORE_FACTORY_TYPE],
            havingValue = "SLEEPY_CAT",
            matchIfMissing = false
        )
        fun quickFixJServerSleepyCatMessageStoreFactory(sessionSettings: SessionSettings): MessageStoreFactory {
            return SleepycatStoreFactory(sessionSettings)
        }
    }

    /**
     * Creates JMX Exporter.
     *
     * @param serverAcceptor The server's [Acceptor]
     * @return The server's JMX bean
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "${PROPERTY_PREFIX}.jmx",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = false
    )
    @ConditionalOnClass(JmxExporter::class)
    @ConditionalOnSingleCandidate(Acceptor::class)
    @ConditionalOnMissingBean(name = ["quickFixJServerJmxExporter"])
    fun quickFixJServerJmxExporter(
        @Suppress("SpringJavaInjectionPointsAutowiringInspection") serverAcceptor: Acceptor
    ): ObjectName {
        return try {
            val exporter = JmxExporter()
            exporter.setRegistrationBehavior(JmxExporter.REGISTRATION_REPLACE_EXISTING)
            exporter.register(serverAcceptor)
        } catch (e: Exception) {
            throw SessionSettingsException(e.message, e)
        }
    }
}