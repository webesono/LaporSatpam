package com.example.laporsatpam.models

import java.util.Date


class ChatMessage {
    var message: String? = null
    var senderId: String? = null
    var receiveId: String? = null
    var timestamp: String? = null
    var image: String? = null

    constructor(){}

    constructor(message: String?, senderId: String?, timestamp: String?){
        this.message = message
        this.senderId = senderId
        this.timestamp = timestamp

    }
}