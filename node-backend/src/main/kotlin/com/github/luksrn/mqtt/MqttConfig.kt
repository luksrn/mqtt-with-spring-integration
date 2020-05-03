package com.github.luksrn.mqtt

import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.springframework.beans.factory.annotation.Value
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

    @Value("\${mqtt.broker}")
    lateinit var brokerServerURI: String

    @Bean
    fun mqttPahoClientFactory(): MqttPahoClientFactory {
        val factory = DefaultMqttPahoClientFactory()
        val options = MqttConnectOptions().apply {
            serverURIs = arrayOf(brokerServerURI)
        }
        factory.connectionOptions = options
        return factory
    }

    @Bean
    fun nodeRemoteCommandFlow(): IntegrationFlow {
        return IntegrationFlows
                .from("simpleCommandMessageChannel")
                .transform(Transformers.toJson())
                .handle(mqttNodeOutboundMessageHandler())
                .get()
    }

    @Bean
    fun mqttNodeOutboundMessageHandler(): MessageHandler {
        return MqttPahoMessageHandler(
                "server-node-${InetAddress.getLocalHost().hostName}",
                mqttPahoClientFactory()).apply {
            setAsync(true)
            setTopicExpressionString("headers['node_id']")
            setDefaultTopic("node-broadcast")
        }
    }

    @Bean
    fun mqttInboundNodeFlow(): IntegrationFlow {
        return IntegrationFlows.from(mqttInboundNodeMessageProducer())
                .handle(MessageHandler(::println))
                .get()
    }

    @Bean
    fun mqttInboundNodeMessageProducer(): MessageProducerSupport {
        return MqttPahoMessageDrivenChannelAdapter(
                "remote-node-inbound",
                mqttPahoClientFactory(),
                "node-health").apply {
            setCompletionTimeout(5000)
        }
    }
}