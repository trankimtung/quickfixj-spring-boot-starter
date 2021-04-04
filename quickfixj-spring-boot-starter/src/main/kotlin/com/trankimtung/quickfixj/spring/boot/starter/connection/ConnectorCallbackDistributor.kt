package com.trankimtung.quickfixj.spring.boot.starter.connection

import quickfix.Application
import quickfix.Message
import quickfix.SessionID

/**
 * An [Application] implementation that will forward event to given [ConnectorCallback] list.
 */
class ConnectorCallbackDistributor(
    private val callbacks: List<ConnectorCallback>
) : Application {

    private val noopConnectorCallback = NoopConnectorCallback()

    override fun onCreate(sessionId: SessionID?) =
        getCallBack(sessionId).onCreate(sessionId)

    override fun onLogon(sessionId: SessionID?) =
        getCallBack(sessionId).onLogon(sessionId)

    override fun onLogout(sessionId: SessionID?) =
        getCallBack(sessionId).onLogout(sessionId)

    override fun toAdmin(message: Message?, sessionId: SessionID?) =
        getCallBack(sessionId).toAdmin(message, sessionId)

    override fun fromAdmin(message: Message?, sessionId: SessionID?) =
        getCallBack(sessionId).fromAdmin(message, sessionId)

    override fun toApp(message: Message?, sessionId: SessionID?) =
        getCallBack(sessionId).toApp(message, sessionId)

    override fun fromApp(message: Message?, sessionId: SessionID?) =
        getCallBack(sessionId).fromApp(message, sessionId)

    /**
     * Get a suitable [ConnectorCallback] to process events from given session.
     *
     * @param sessionID event's source session id.
     * @return target [ConnectorCallback], [NoopConnectorCallback] if no others can be found.
     */
    internal fun getCallBack(sessionID: SessionID?): ConnectorCallback {
        return callbacks.firstOrNull {
            it.supportsSessionId(sessionID)
        } ?: noopConnectorCallback
    }
}