package com.example.foodie

object CartRepository {
    val items = mutableListOf<CartItem>()

    fun addItem(item: CartItem) {
        val existingItem = items.find { it.itemName == item.itemName }
        if (existingItem != null) {
            existingItem.quantity += item.quantity
        } else {
            items.add(item)
        }
    }

    fun getCartTotal(): Double {
        return items.sumOf { it.itemPrice * it.quantity }
    }

    fun clearCart() {
        items.clear()
    }
}