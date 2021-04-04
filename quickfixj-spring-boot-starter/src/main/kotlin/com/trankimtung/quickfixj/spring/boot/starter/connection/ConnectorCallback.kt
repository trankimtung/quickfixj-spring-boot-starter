package com.trankimtung.quickfixj.spring.boot.starter.connection

import quickfix.Application
import quickfix.SessionID

/**
 * This interface is used for processing QuickFIX/J events for a certain session.
 *
 * @see [Application]
 */
interface ConnectorCallback : Application {

    /**
     * Get the session id whose events should be processed by this [ConnectorCallback].
     */
    fun getSessionId(): SessionID

    /**
     * Checks if this [ConnectorCallback] should receive events for given session id.
     *
     * @param sessionId event's source session id.
     */
    fun supportsSessionId(sessionId: SessionID?): Boolean =
        sessionId?.equals(getSessionId()) ?: false
}