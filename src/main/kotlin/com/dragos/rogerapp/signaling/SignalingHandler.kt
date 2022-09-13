package com.dragos.rogerapp.signaling

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SignalingHandler : TextWebSocketHandler() {
    private val logger = KotlinLogging.logger {}
    private val jacksonMapper = jacksonObjectMapper()

    private val sessionMap: ConcurrentHashMap<String, WebSocketUserSession> = ConcurrentHashMap()

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        super.handleTextMessage(session, message)

        val data = jacksonMapper.readValue(message.payload, SignalData::class.java)

        logger.debug { data.toString() }

        when (data.type) {
            SignalType.Offer -> handleOffer(data, session)
            SignalType.Answer -> handleAnswer(data, session)
            SignalType.Ice -> handleIce(data, session)
            else -> {
                // no-op
            }
        }
    }

    private fun handleNewUserConnection(session: WebSocketSession) {
        //generate userId and assign it to the session data
        val userSession = WebSocketUserSession(session = session, userId = UUID.randomUUID().toString())
        sessionMap[session.id] = userSession

        userSession.session.send(
            SignalData(
                type = SignalType.Welcome,
                data = "Welcome ${userSession.userId}"
            )
        )
        // send a message to all other users that a new user has connected
        sessionMap
            .filterNot {
                it.key == userSession.session.id
            }
            .forEach { entry ->
                val peerSession = entry.value
                peerSession.session.send(
                    SignalData(
                        fromUID = userSession.userId,
                        type = SignalType.OfferRequest
                    )
                )
            }
    }

    /**
     * directly proxies a message from one user to another
     */
    private fun proxyMessage(data: SignalData, session: WebSocketSession) {
        val userSession = sessionMap[session.id] ?: return
        try {
            val peerUserSession = sessionMap.values.first { s ->
                s.userId == data.toUID
            }

            peerUserSession.session.send(
                data.copy(
                    fromUID = userSession.userId,
                    toUID = null
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleOffer(data: SignalData, session: WebSocketSession) {
        proxyMessage(data, session)
    }

    private fun handleAnswer(data: SignalData, session: WebSocketSession) {
        proxyMessage(data, session)
    }

    private fun handleIce(data: SignalData, session: WebSocketSession) {
        proxyMessage(data, session)
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        super.afterConnectionEstablished(session)
        logger.debug { "afterConnectionEstablished" }

        handleNewUserConnection(session)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        super.afterConnectionClosed(session, status)
        logger.debug { "afterConnectionClosed" }

        sessionMap.remove(session.id)
    }


    private fun WebSocketSession.send(data: SignalData) {
        try {
            if (isOpen) {
                sendMessage(
                    TextMessage(jacksonMapper.writeValueAsString(data))
                )
            } else {
                logger.error { "Cannot send message because session with $id is closed. " }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}