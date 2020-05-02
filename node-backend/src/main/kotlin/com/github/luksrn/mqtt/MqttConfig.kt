package com.github.luksrn.mqtt

import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.Transformers
import org.springframework.integration.endpoint.MessageProducerSupport
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory
import org.springframework.integration.mqtt.core.MqttPahoClientFactory
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler
import org.springframework.messaging.MessageHandler
import java.net.InetAddress


@Configuration
class MqttConfig {

    @Bean
    fun mqttClientFactory(): MqttPahoClientFactory {
        val factory = DefaultMqttPahoClientFactory()
        factory
        val options = MqttConnectOptions().apply {
            serverURIs = arrayOf("tcp://emqx:1883")
        }
        factory.connectionOptions = options
        return factory
    }

    @Bean
    fun nodeRemoteCommandFlow(): IntegrationFlow {
        return IntegrationFlows
                .from("integration.node.gateway.channel")
                .transform(Transformers.toJson())
                .handle(nodeOutbound())
                .get()
    }

    @Bean
    fun nodeOutbound(): MessageHandler {
        return MqttPahoMessageHandler(
                "server-node-${InetAddress.getLocalHost().hostName}",
                mqttClientFactory()).apply {
            setAsync(true)
            setDefaultTopic("node-broadcast")
        }
    }

    @Bean
    fun inboundNodeFlow(): IntegrationFlow {
        return IntegrationFlows.from(inboundNodeMessageProducer())
                .handle(MessageHandler(::println))
                .get()
    }

    @Bean
    fun inboundNodeMessageProducer(): MessageProducerSupport {
        return MqttPahoMessageDrivenChannelAdapter(
                "remote-node-inbound",
                mqttClientFactory(),
                 "node-health").apply {
            setCompletionTimeout(5000)
        }
    }
}