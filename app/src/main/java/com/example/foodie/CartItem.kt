package com.example.foodie

import com.google.firebase.firestore.IgnoreExtraProperties

// Making the class more Firestore-friendly
@IgnoreExtraProperties
data class CartItem(
    @JvmField var itemName: String = "",
    @JvmField var itemPrice: Double = 0.0,
    @JvmField var quantity: Int = 0,
    @JvmField var itemImage: String = ""
)
