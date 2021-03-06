package com.trankimtung.quickfixj.spring.boot.starter.autoconfigure.server

import com.trankimtung.quickfixj.spring.boot.starter.connection.*
import com.trankimtung.quickfixj.spring.boot.starter.connection.LogFactoryType.*
import com.trankimtung.quickfixj.spring.boot.starter.exception.SessionSettingsException
import org.quickfixj.jmx.JmxExporter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
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

        const val BEAN_APPLICATION = "quickFixJServerApplication"
        const val BEAN_SESSION_SETTINGS = "quickFixJServerSessionSettings"
        const val BEAN_MESSAGE_FACTORY = "quickFixJServerMessageFactory"
        const val BEAN_MESSAGE_STORE_FACTORY = "quickFixJServerMessageStoreFactory"
        const val BEAN_LOG_FACTORY = "quickFixJServerLogFactory"
        const val BEAN_SOCKET_ACCEPTOR = "quickFixJServerSocketAcceptor"
        const val BEAN_CONNECTOR_MANAGER = "quickFixJServerConnectorManager"
        const val BEAN_JMX_EXPORTER = "quickFixJServerJmxExporter"

        private const val PROPERTY_MESSAGE_STORE_FACTORY_TYPE = "message-store-factory.type"
    }

    /**
     * Loads the [Connector] session settings from config file.
     * Search order:
     * - Location defined in application properties.
     * - quickfixj-server.cfg in working directory.
     * - quickfixj-server.cfg in classpath.
     */
    @Bean(BEAN_SESSION_SETTINGS)
    @ConditionalOnMissingBean(name = [BEAN_SESSION_SETTINGS])
    fun sessionSettings(properties: QuickFixJBootProperties): SessionSettings {
        return SessionSettingsLoader.loadSettings(
            properties.server.config,
            "file:./${QUICKFIXJ_SERVER_CONFIG_FILENAME}",
            "classpath:/${QUICKFIXJ_SERVER_CONFIG_FILENAME}"
        )
    }

    /**
     * Creates a [MessageFactory].
     */
    @Bean(BEAN_MESSAGE_FACTORY)
    @ConditionalOnMissingBean(name = [BEAN_MESSAGE_FACTORY])
    fun messageFactory(): MessageFactory {
        return DefaultMessageFactory()
    }

    /**
     * Creates a [ConnectorCallbackDistributor] that forwards events to other [ConnectorCallback] beans.
     */
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Bean(BEAN_APPLICATION)
    @ConditionalOnMissingBean(name = [BEAN_APPLICATION])
    fun application(
        connectorCallbacks: List<ConnectorCallback>
    ): Application {
        return ConnectorCallbackDistributor(connectorCallbacks)
    }

    /**
     * Creates a single-threaded [Acceptor].
     *
     * @param application         [Application]
     * @param messageStoreFactory [MessageStoreFactory]
     * @param sessionSettings     [SessionSettings]
     * @param logFactory          [LogFactory]
     * @param messageFactory      [MessageFactory]
     * @return [Acceptor]
     * @throws ConfigError when a configuration error is detected.
     */
    @Bean(BEAN_SOCKET_ACCEPTOR)
    @ConditionalOnMissingBean(name = [BEAN_SOCKET_ACCEPTOR])
    @ConditionalOnProperty(
        prefix = "${PROPERTY_PREFIX}.concurrent",
        name = ["enabled"],
        havingValue = "false",
        matchIfMissing = true
    )
    @Throws(ConfigError::class)
    fun socketAcceptor(
        @Qualifier(BEAN_APPLICATION) application: Application,
        @Qualifier(BEAN_MESSAGE_STORE_FACTORY) messageStoreFactory: MessageStoreFactory,
        @Qualifier(BEAN_SESSION_SETTINGS) sessionSettings: SessionSettings,
        @Qualifier(BEAN_LOG_FACTORY) logFactory: LogFactory,
        @Qualifier(BEAN_MESSAGE_FACTORY) messageFactory: MessageFactory
    ): Acceptor {
        return SocketAcceptor.newBuilder()
            .withApplication(application)
            .withMessageStoreFactory(messageStoreFactory)
            .withSettings(sessionSettings)
            .withLogFactory(logFactory)
            .withMessageFactory(messageFactory)
            .build()
    }

    /**
     * Creates a multi-threaded [Acceptor].
     *
     * @param application         [Application]
     * @param messageStoreFactory       [MessageStoreFactory]
     * @param sessionSettings     [SessionSettings]
     * @param logFactory          [LogFactory]
     * @param messageFactory      [MessageFactory]
     * @return [Acceptor]
     * @throws ConfigError when a configuration error is detected.
     */
    @Bean(BEAN_SOCKET_ACCEPTOR)
    @ConditionalOnMissingBean(name = [BEAN_SOCKET_ACCEPTOR])
    @ConditionalOnProperty(
        prefix = "${PROPERTY_PREFIX}.concurrent",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = false
    )
    @Throws(ConfigError::class)
    fun threadedSocketAcceptor(
        @Qualifier(BEAN_APPLICATION) application: Application,
        @Qualifier(BEAN_MESSAGE_STORE_FACTORY) messageStoreFactory: MessageStoreFactory,
        @Qualifier(BEAN_SESSION_SETTINGS) sessionSettings: SessionSettings,
        @Qualifier(BEAN_LOG_FACTORY) logFactory: LogFactory,
        @Qualifier(BEAN_MESSAGE_FACTORY) messageFactory: MessageFactory
    ): Acceptor {
        return ThreadedSocketAcceptor.newBuilder()
            .withApplication(application)
            .withMessageStoreFactory(messageStoreFactory)
            .withSettings(sessionSettings)
            .withLogFactory(logFactory)
            .withMessageFactory(messageFactory)
            .build()
    }

    /**
     * Setups [ConnectorManager].
     */
    @Bean(BEAN_CONNECTOR_MANAGER)
    fun connectorManager(
        @Qualifier(BEAN_SOCKET_ACCEPTOR) acceptor: Acceptor,
        properties: QuickFixJBootProperties
    ): ConnectorManager {
        return with(properties.server) {
            ConnectorManager(acceptor, autoStartup, startupPhase, forceDisconnect)
        }
    }

    /**
     * Setups a [CompositeLogFactory] that wraps all factories defined in configuration.
     */
    @Bean(BEAN_LOG_FACTORY)
    @ConditionalOnMissingBean(name = [BEAN_LOG_FACTORY])
    fun logFactory(
        @Qualifier(BEAN_SESSION_SETTINGS) sessionSettings: SessionSettings,
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

        @Bean(name = [BEAN_MESSAGE_STORE_FACTORY])
        @ConditionalOnMissingBean(name = [BEAN_MESSAGE_STORE_FACTORY])
        @ConditionalOnProperty(
            prefix = PROPERTY_PREFIX,
            name = [PROPERTY_MESSAGE_STORE_FACTORY_TYPE],
            havingValue = "NOOP",
            matchIfMissing = false
        )
        fun noopMessageStoreFactory(): MessageStoreFactory {
            return NoopStoreFactory()
        }

        @Bean(name = [BEAN_MESSAGE_STORE_FACTORY])
        @ConditionalOnMissingBean(name = [BEAN_MESSAGE_STORE_FACTORY])
        @ConditionalOnProperty(
            prefix = PROPERTY_PREFIX,
            name = [PROPERTY_MESSAGE_STORE_FACTORY_TYPE],
            havingValue = "MEMORY",
            matchIfMissing = true
        )
        fun memoryMessageStoreFactory(): MessageStoreFactory {
            return MemoryStoreFactory()
        }

        @Bean(name = [BEAN_MESSAGE_STORE_FACTORY])
        @ConditionalOnMissingBean(name = [BEAN_MESSAGE_STORE_FACTORY])
        @ConditionalOnProperty(
            prefix = PROPERTY_PREFIX,
            name = [PROPERTY_MESSAGE_STORE_FACTORY_TYPE],
            havingValue = "JDBC",
            matchIfMissing = false
        )
        fun jdbcMessageStoreFactory(
            @Qualifier(BEAN_SESSION_SETTINGS) sessionSettings: SessionSettings,
            properties: QuickFixJBootProperties,
            datasource: DataSource?
        ): MessageStoreFactory {
            return JdbcStoreFactory(sessionSettings).apply {
                if (properties.server.messageStoreFactory.useSpringJdbc) {
                    setDataSource(datasource ?: throw IllegalStateException("DataSource not set."))
                }
            }
        }

        @Bean(name = [BEAN_MESSAGE_STORE_FACTORY])
        @ConditionalOnMissingBean(name = [BEAN_MESSAGE_STORE_FACTORY])
        @ConditionalOnProperty(
            prefix = PROPERTY_PREFIX,
            name = [PROPERTY_MESSAGE_STORE_FACTORY_TYPE],
            havingValue = "FILE",
            matchIfMissing = false
        )
        fun fileMessageStoreFactory(
            @Qualifier(BEAN_SESSION_SETTINGS) sessionSettings: SessionSettings
        ): MessageStoreFactory {
            return FileStoreFactory(sessionSettings)
        }

        @Bean(name = [BEAN_MESSAGE_STORE_FACTORY])
        @ConditionalOnMissingBean(name = [BEAN_MESSAGE_STORE_FACTORY])
        @ConditionalOnProperty(
            prefix = PROPERTY_PREFIX,
            name = [PROPERTY_MESSAGE_STORE_FACTORY_TYPE],
            havingValue = "CACHED_FILE",
            matchIfMissing = false
        )
        fun cachedFileMessageStoreFactory(
            @Qualifier(BEAN_SESSION_SETTINGS) sessionSettings: SessionSettings
        ): MessageStoreFactory {
            return CachedFileStoreFactory(sessionSettings)
        }

        @Bean(name = [BEAN_MESSAGE_STORE_FACTORY])
        @ConditionalOnMissingBean(name = [BEAN_MESSAGE_STORE_FACTORY])
        @ConditionalOnProperty(
            prefix = PROPERTY_PREFIX,
            name = [PROPERTY_MESSAGE_STORE_FACTORY_TYPE],
            havingValue = "SLEEPY_CAT",
            matchIfMissing = false
        )
        fun sleepyCatMessageStoreFactory(
            @Qualifier(BEAN_SESSION_SETTINGS) sessionSettings: SessionSettings
        ): MessageStoreFactory {
            return SleepycatStoreFactory(sessionSettings)
        }
    }

    /**
     * Creates JMX Exporter.
     *
     * @param acceptor The server's [Acceptor]
     * @return The server's JMX bean
     */
    @Bean(BEAN_JMX_EXPORTER)
    @ConditionalOnProperty(
        prefix = "${PROPERTY_PREFIX}.jmx",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = false
    )
    @ConditionalOnClass(JmxExporter::class)
    @ConditionalOnMissingBean(name = [BEAN_JMX_EXPORTER])
    fun jmxExporter(
        @Qualifier(BEAN_SOCKET_ACCEPTOR) acceptor: Acceptor
    ): ObjectName {
        return try {
            val exporter = JmxExporter()
            exporter.setRegistrationBehavior(JmxExporter.REGISTRATION_REPLACE_EXISTING)
            exporter.register(acceptor)
        } catch (e: Exception) {
            throw SessionSettingsException(e.message, e)
        }
    }
}