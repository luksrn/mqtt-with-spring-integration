package com.github.luksrn.mqtt

import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.integration.channel.FluxMessageChannel
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.Transformers
import org.springframework.integration.endpoint.MessageProducerSupport
import org.springframework.integration.expression.FunctionExpression
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory
import org.springframework.integration.mqtt.core.MqttPahoClientFactory
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler
import org.springframework.integration.mqtt.support.MqttHeaders
import org.springframework.integration.webflux.dsl.WebFlux
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.support.GenericMessage
import reactor.core.publisher.Mono
import java.net.InetAddress
import java.util.function.Function


@Configuration
class MqttConfig {

    @Bean
    fun sendBroadcastMessageToNodes(): IntegrationFlow? {
        return IntegrationFlows
                .from(WebFlux.inboundGateway("/send-broadcast")
                        .requestMapping { m ->
                            m.produces(MediaType.APPLICATION_JSON_VALUE)
                            m.methods(HttpMethod.POST)
                        }
                        .requestPayloadType(SimpleCommand::class.java)
                )
                .channel(fluxMessageChannel())
                .handle { p: SimpleCommand, _ -> Mono.just(p) }
                .get()
    }

    @Bean
    fun sendDirectMessageToNode(): IntegrationFlow? {
        return IntegrationFlows
                .from(WebFlux.inboundGateway("/send-to-node/{id}")
                        .requestMapping { m ->
                            m.produces(MediaType.APPLICATION_JSON_VALUE)
                            m.methods(HttpMethod.POST)
                        }
                        .headerExpression("node_id", "'node-' + #pathVariables.id")
                        .requestPayloadType(SimpleCommand::class.java))
                .channel(fluxMessageChannel())
                .handle { p: SimpleCommand, _ -> Mono.just(p) }
                .get()
    }


    @Bean
    fun mqttClientFactory(): MqttPahoClientFactory {
        val factory = DefaultMqttPahoClientFactory()
        val options = MqttConnectOptions().apply {
            serverURIs = arrayOf("tcp://emqx:1883")
        }
        factory.connectionOptions = options
        return factory
    }

    @Bean("simpleCommandMessageChannel")
    fun fluxMessageChannel() :MessageChannel {
        return FluxMessageChannel()
    }

    @Bean
    fun nodeRemoteCommandFlow(): IntegrationFlow {
        return IntegrationFlows
                .from("simpleCommandMessageChannel")
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
            setTopicExpressionString("headers['node_id']")
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