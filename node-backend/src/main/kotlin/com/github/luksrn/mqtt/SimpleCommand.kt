package com.github.luksrn.mqtt

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class SimpleCommand @JsonCreator constructor (
        @JsonProperty("message") val message: String
)