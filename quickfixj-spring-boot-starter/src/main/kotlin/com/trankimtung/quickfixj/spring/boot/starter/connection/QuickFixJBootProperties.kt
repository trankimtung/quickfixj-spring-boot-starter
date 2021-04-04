package com.trankimtung.quickfixj.spring.boot.starter.connection

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * QuickFix/J starter properties.
 */
@ConstructorBinding
@ConfigurationProperties(prefix = QuickFixJBootProperties.PROPERTY_PREFIX)
class QuickFixJBootProperties {

    companion object {
        const val PROPERTY_PREFIX = "kt.quickfixj"
    }

    /**
     * Client properties.
     */
    var client: ConnectorConfig = ConnectorConfig()

    /**
     * Server properties.
     */
    var server: ConnectorConfig = ConnectorConfig()
}