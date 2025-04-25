package com.example.test.model

data class Chatting(
    var type: String,
    var from: String,
    var to: String,
    var content: String,
    var sendTime: Long
)
