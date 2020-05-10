package com.github.luksrn.mqtt.node

import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.Pollers
import org.springframework.integration.dsl.Transformers
import org.springframework.integration.endpoint.MessageProducerSupport
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory
import org.springframework.integration.mqtt.core.MqttPahoClientFactory
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler
import org.springframework.messaging.MessageHandler
import java.util.UUID
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.random.Random
import kotlin.random.nextInt


@Configuration
class MqttConfig {

    var id : String = UUID.randomUUID().toString()

    @Value("\${mqtt.broker}")
    lateinit var brokerServerURI: String

    @Bean
    fun mqttClientFactory(): MqttPahoClientFactory {
            val factory = DefaultMqttPahoClientFactory()
        val options = MqttConnectOptions().apply {
            serverURIs = arrayOf(brokerServerURI)
        }
        factory.connectionOptions = options
        return factory
    }

    @Bean
    fun mqttInboundServerSimpleCommandFlow(): IntegrationFlow {
        return IntegrationFlows.from(nodeInboundServerCommandMessageProducer())
                .handle(MessageHandler(::println))
                .get()
    }

    @Bean
    fun nodeInboundServerCommandMessageProducer(): MessageProducerSupport {
        return MqttPahoMessageDrivenChannelAdapter(
                "node-${id}-inbound",
                mqttClientFactory(),
                "nodes/${id}/commands", "nodes/broadcast").apply {
            setCompletionTimeout(5000)
        }
    }

    @Bean
    fun mqttOutboundPushHealthFlow(): IntegrationFlow? {
        return IntegrationFlows.from(
                    Supplier { NodeHeathStatus(id, true, Random.nextInt(IntRange(90, 100))) },
                    Consumer { p -> p.poller(Pollers.fixedRate(30_000).maxMessagesPerPoll(1)) }
                )
                .log()
                .transform(Transformers.toJson())
                .handle(serverHealthOutbound())
                .get()
    }

    @Bean
    fun serverHealthOutbound(): MessageHandler {
        return MqttPahoMessageHandler(
                "node-${id}",
                mqttClientFactory()).apply {
            setAsync(true)
            setDefaultTopic("nodes/${id}/health")
        }
    }
}
data class NodeHeathStatus(val id: String, val connection: Boolean, val battery: Int)