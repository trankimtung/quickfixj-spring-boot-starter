package com.trankimtung.quickfixj.spring.boot.starter.connection

import com.trankimtung.quickfixj.spring.boot.starter.exception.SessionSettingsException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
import quickfix.ConfigError
import quickfix.SessionSettings
import java.io.IOException

class SessionSettingsLoader {
    companion object {

        private val log: Logger = LoggerFactory.getLogger(SessionSettingsLoader::class.java)

        /**
         * Loads QuickFix/J SessionSettings from given locations.
         *
         * @param locations search locations.
         * @return [SessionSettings] object.
         * @throws [SessionSettingsException] if the configuration malfunctions or does not exist.
         */
        fun loadSettings(vararg locations: String?): SessionSettings {
            try {
                for (location in locations) {
                    val resource = load(location)
                    if (resource != null) {
                        log.info("Found QuickFix/J configuration at '{}'", location)
                        return SessionSettings(resource.inputStream)
                    }
                }
                throw SessionSettingsException("QuickFix/J configuration not found.")
            } catch (e: Throwable) {
                when (e) {
                    is ConfigError, is IOException -> throw SessionSettingsException(e.message, e)
                    else -> throw e
                }
            }
        }

        /**
         * Loads resources from given location.
         */
        private fun load(location: String?): Resource? {
            return if (location == null) {
                null
            } else {
                val classLoader = Thread.currentThread().contextClassLoader
                val resolver: ResourcePatternResolver = PathMatchingResourcePatternResolver(classLoader)
                val resource = resolver.getResource(location)
                return if (resource.exists()) resource else null
            }
        }
    }
}