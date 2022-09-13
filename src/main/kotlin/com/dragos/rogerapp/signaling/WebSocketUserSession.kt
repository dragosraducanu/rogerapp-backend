package com.dragos.rogerapp.signaling

import org.springframework.web.socket.WebSocketSession

data class WebSocketUserSession(
    val session: WebSocketSession,
    val userId: String
)