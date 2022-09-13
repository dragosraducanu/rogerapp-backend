package com.dragos.rogerapp.signaling

data class SignalData(
    val fromUID: String? = null,
    val type: SignalType,
    val data: String? = null,
    val toUID: String? = null
)

enum class SignalType {
    /**
     * requests an offer from a client
     */
   OfferRequest,

    /**
     * sent by a user whenever they want to offer a connection
     */
    Offer,

    /**
     * sent by a user whenever they want to answer an offer
     */
    Answer,

    Ice,

    /**
     * Connection established message
     */
    Welcome,
}