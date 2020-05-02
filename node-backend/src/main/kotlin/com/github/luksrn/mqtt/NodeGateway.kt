package com.github.luksrn.mqtt

import org.springframework.integration.annotation.Gateway
import org.springframework.integration.annotation.MessagingGateway
import org.springframework.integration.mqtt.support.MqttHeaders
import org.springframework.messaging.handler.annotation.Header

@MessagingGateway
interface NodeGateway {

    @Gateway(requestChannel = "integration.node.gateway.channel")
    fun sendBroadcast(data: SimpleCommand)

    @Gateway(requestChannel = "integration.node.gateway.channel")
    fun sendToBox(@Header(value = MqttHeaders.TOPIC) topic: String, data: SimpleCommand)
}