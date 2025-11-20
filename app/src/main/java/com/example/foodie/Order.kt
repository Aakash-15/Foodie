package com.example.foodie

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

// Making the class more Firestore-friendly
@IgnoreExtraProperties
data class Order(
    @JvmField var orderId: String = "",
    @get:PropertyName("items") @set:PropertyName("items") @JvmField var items: List<CartItem> = emptyList(),
    @JvmField var totalAmount: Double = 0.0,
    @JvmField var address: String = "",
    @JvmField var orderStatus: String = "",
    @JvmField var userId: String = ""
)
