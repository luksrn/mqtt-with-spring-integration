# spring-integration-mqtt
Simple project using Spring Integration MQTT and writen using Kotlin with the interest in learning and practice.

It consists of a backend with capacities to sending messages to the MQTT broker, to reach all nodes or to a specific one. Also, it receives health information about each node every 30 seconds.

# Running locally with kind

$ kind create cluster
$ k apply -k k8s

# To see the logs [blocks the terminal]
$ stern node

# to expose services locally [blocks the terminal]
$ k port-forward deployment/emqx 18083:18083
$ k port-forward deployment/mqtt-spring-integration-node-backend 8080:8080

$ # port-forward:

# Send a message to node with id = a776a73c-74b3-36ed-fbe6-c64ddc7bd262 (look on logs)
http :8080/send-to-node node==a776a73c-74b3-36ed-fbe6-c64ddc7bd262

# Send a broadcast message
http :8080/send-broadcast

