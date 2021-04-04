package com.trankimtung.quickfixj.spring.boot.starter.connection

import org.slf4j.LoggerFactory
import quickfix.Message
import quickfix.SessionID

/**
 * A [ConnectorCallback] implementation that does nothing but logs the session id and message at DEBUG level.
 */
class NoopConnectorCallback : ConnectorCallback {

    companion object {
        private val log = LoggerFactory.getLogger(NoopConnectorCallback::class.java)
    }

    override fun getSessionId(): SessionID = SessionID("FIX4.4", "NOOP", "NOOP")

    override fun onCreate(sessionId: SessionID) {
        log.debug("onCreate: $sessionId")
    }

    override fun onLogon(sessionId: SessionID) {
        log.debug("onLogon: $sessionId")
    }

    override fun onLogout(sessionId: SessionID) {
        log.debug("onLogout: $sessionId")
    }

    override fun toAdmin(message: Message, sessionId: SessionID) {
        log.debug("toAdmin: $sessionId, message: $message")
    }

    override fun fromAdmin(message: Message, sessionId: SessionID) {
        log.debug("fromAdmin: $sessionId, message: $message")
    }

    override fun toApp(message: Message, sessionId: SessionID) {
        log.debug("toApp: $sessionId, message: $message")
    }

    override fun fromApp(message: Message, sessionId: SessionID) {
        log.debug("fromApp: $sessionId, message: $message")
    }
}