package com.github.luksrn.mqtt.node

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NodeClientApplication

fun main(args: Array<String>) {
	runApplication<NodeClientApplication>(*args)
}
