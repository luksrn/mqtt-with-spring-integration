package com.github.luksrn.mqtt.node

import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler

class NodeClientMessageHandler : MessageHandler {

    override fun handleMessage(message: Message<*>) {
        println("Receive command ${message.payload}")
    }
}