package com.douralfourkan.firstapp

data class Message(
    val id: Int,
    val sender: String,
    val content: String,
    val timestamp: Long
)