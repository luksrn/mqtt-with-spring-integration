package com.github.luksrn.mqtt

import java.time.Instant

data class SimpleCommand (val payload: String, val createdAt : Instant = Instant.now())