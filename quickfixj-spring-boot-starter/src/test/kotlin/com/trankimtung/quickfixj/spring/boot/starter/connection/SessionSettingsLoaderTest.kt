package com.trankimtung.quickfixj.spring.boot.starter.connection

import com.trankimtung.quickfixj.spring.boot.starter.exception.SessionSettingsException
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertNotNull

internal class SessionSettingsLoaderTest {

    @Test
    fun `Throws SessionSettingsException if not found`() {
        assertThrows<SessionSettingsException> {
            SessionSettingsLoader.loadSettings(null, null)
        }
    }

    @Test
    fun `Can load from classpath`() {
        val settings = SessionSettingsLoader.loadSettings("classpath:quickfixj-server.cfg", null)
        assertNotNull(settings)
    }
}