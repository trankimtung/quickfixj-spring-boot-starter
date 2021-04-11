package com.trankimtung.quickfixj.spring.boot.starter.connection

import org.mockito.BDDMockito
import org.mockito.Mockito
import quickfix.SessionID
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ConnectorCallbackDistributorTest {

    @Test
    fun `Returns NOOP connector callback by default`() {
        val distributor = ConnectorCallbackDistributor(emptyList())
        val sessionID = SessionID("FIX4.4", "EXEC", "BANZAI")
        assertEquals(NoopConnectorCallback::class, distributor.getCallBack(sessionID)::class)
    }

    @Test
    fun `Returns connector callback by session ID`() {
        val sessionID = SessionID("FIX4.4", "EXEC", "BANZAI")
        val callbacks = listOf(
            Mockito.spy(ConnectorCallback::class.java).apply {
                BDDMockito.willReturn(sessionID).given(this).getSessionId()
                BDDMockito.willReturn(true).given(this).supportsSessionId(sessionID)
            },
            Mockito.spy(ConnectorCallback::class.java).apply {
                val sessionID42 = SessionID("FIX4.2", "EXEC", "BANZAI")
                BDDMockito.willReturn(sessionID42).given(this).getSessionId()
                BDDMockito.willReturn(true).given(this).supportsSessionId(sessionID42)
            },
            Mockito.spy(ConnectorCallback::class.java).apply {
                val sessionID11 = SessionID("FIX1.1", "EXEC", "BANZAI")
                BDDMockito.willReturn(sessionID11).given(this).getSessionId()
                BDDMockito.willReturn(true).given(this).supportsSessionId(sessionID11)
            }
        )
        val distributor = ConnectorCallbackDistributor(callbacks)
        assertEquals(sessionID, distributor.getCallBack(sessionID).getSessionId())
    }

    @Test
    fun `Calls correct callbacks on events`() {
        val sessionID = SessionID("FIX4.4:EXEC->BANZAI")
        val callback = Mockito.mock(ConnectorCallback::class.java)
        BDDMockito.willReturn(sessionID).given(callback).getSessionId()
        BDDMockito.willReturn(true).given(callback).supportsSessionId(sessionID)
        val distributor = ConnectorCallbackDistributor(listOf(callback))

        with(distributor) {
            onCreate(sessionID)
            onLogon(sessionID)
            onLogout(sessionID)
            toAdmin(null, sessionID)
            fromAdmin(null, sessionID)
            toApp(null, sessionID)
            fromApp(null, sessionID)
        }

        with(Mockito.verify(callback)) {
            onCreate(sessionID)
            onLogon(sessionID)
            onLogout(sessionID)
            toAdmin(null, sessionID)
            fromAdmin(null, sessionID)
            toApp(null, sessionID)
            fromApp(null, sessionID)
        }
    }
}