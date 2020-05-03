# spring-integration-mqtt
Simple project using Spring Integration MQTT and writen using Kotlin with the interest in learning and practice.

It consists of a backend with capacities to sending messages to the MQTT broker, to reach all nodes or to a specific one. Also, it receives health information about each node every 30 seconds.

# Running locally with kind
```
$ kind create cluster
$ k apply -k k8s
```

To see the logs [blocks the terminal]
```
$ stern node
```
to expose services locally [blocks the terminal]
```
$ k port-forward deployment/emqx 18083:18083
$ k port-forward deployment/mqtt-spring-integration-node-backend 8080:8080
```

Send a message to node with id (look on logs)
```
http POST :8080/send-to-node/e32457e6-3756-469b-a5a2-c67414bd2c24 message="Está sendo enviado via flux"
```

Send a broadcast message
```
http POST :8080/send-broadcast message="Broadcast Está sendo enviado via flux"
```

