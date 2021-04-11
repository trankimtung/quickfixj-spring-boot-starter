package com.trankimtung.quickfixj.spring.boot.starter.connection

import com.trankimtung.quickfixj.spring.boot.starter.exception.SessionSettingsException
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito
import org.mockito.Mockito
import quickfix.ConfigError
import quickfix.Connector
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ConnectorManagerTest {

    @Test
    fun `Starts and stops without callback`() {
        val connector = Mockito.mock(Connector::class.java)
        val connectorManager = ConnectorManager(connector)

        connectorManager.run {
            start()
            assertTrue(isRunning)
            stop()
            assertFalse(isRunning)
        }

        Mockito.verify(connector).run {
            start()
            stop(false)
        }
    }

    @Test
    fun `Starts and stops with force connect on`() {
        val connector = Mockito.mock(Connector::class.java)
        val connectorManager = ConnectorManager(connector = connector, forceDisconnect = true)

        connectorManager.run {
            start()
            assertTrue(isRunning)
            stop()
            assertFalse(isRunning)
        }

        Mockito.verify(connector).run {
            start()
            stop(true)
        }
    }

    @Test
    fun `Starts and stops with callback`() {
        val connector = Mockito.mock(Connector::class.java)
        val connectorManager = ConnectorManager(connector)
        val callback = Mockito.mock(Runnable::class.java)

        connectorManager.run {
            start()
            assertTrue(isRunning)
            stop(callback)
            assertFalse(isRunning)
        }

        Mockito.verify(connector).run {
            start()
            stop(false)
        }
        Mockito.verify(callback).run()
    }

    @Test
    fun `Throws SessionSettingsException when connection settings is invalid`() {
        val connector = Mockito.mock(Connector::class.java)
        BDDMockito.willThrow(ConfigError::class.java).given(connector).start()
        val connectorManager = ConnectorManager(connector)

        assertThrows<SessionSettingsException> { connectorManager.start() }
        assertFalse(connectorManager.isRunning)

        Mockito.verify(connector).start()
    }
}