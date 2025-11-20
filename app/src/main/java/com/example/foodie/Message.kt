package com.example.foodie

data class Message(
    val text: String,
    val type: Int
) {
    companion object {
        const val TYPE_USER = 0
        const val TYPE_BOT = 1
    }
}