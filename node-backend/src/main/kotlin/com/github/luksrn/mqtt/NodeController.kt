package com.github.luksrn.mqtt

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class NodeController(val nodeGateway: NodeGateway) {

    @GetMapping("/send-to-node")
    fun sendToNode(@RequestParam("node") nodeId : String) =
            nodeGateway.sendToBox( "node-${nodeId}", SimpleCommand("Send from backend to node $nodeId"))

    @GetMapping("/send-broadcast")
    fun sendBroadcast() = nodeGateway.sendBroadcast(SimpleCommand("Send broadcast to nodes"))
}