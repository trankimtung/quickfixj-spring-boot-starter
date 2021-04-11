package com.trankimtung.quickfixj.spring.boot.starter.connection

import org.junit.Test
import kotlin.test.assertNotNull

class NoopConnectorCallbackTest {

    private val callback = NoopConnectorCallback()

    @Test
    fun `Session ID is not null`() {
        assertNotNull(callback.getSessionId())
    }
}