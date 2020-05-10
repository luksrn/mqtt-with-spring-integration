package com.github.luksrn.mqtt

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.integration.channel.FluxMessageChannel
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.webflux.dsl.WebFlux
import org.springframework.messaging.MessageChannel
import reactor.core.publisher.Mono

@Configuration
class FluxConfig {

    @Bean("simpleCommandMessageChannel")
    fun simpleCommandMessageChannel() : MessageChannel {
        return FluxMessageChannel()
    }

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
                .channel(simpleCommandMessageChannel())
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
                        .headerExpression("node_id", "'nodes/' + #pathVariables.id + '/commands'")
                        .requestPayloadType(SimpleCommand::class.java))
                .log()
                .channel(simpleCommandMessageChannel())
                .handle { p: SimpleCommand, _ -> Mono.just(p) }
                .get()
    }
}