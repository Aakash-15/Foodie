package com.example.foodie

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val text: String? = null,
    val combo: Combo? = null,
    val type: Int
) {
    companion object {
        const val TYPE_USER = 0
        const val TYPE_BOT = 1
        const val TYPE_COMBO = 2
    }
}

@Serializable
data class Combo(
    val introText: String,
    val items: List<FoodItem>,
    val totalPrice: Double
)