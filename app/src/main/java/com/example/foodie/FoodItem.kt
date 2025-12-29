package com.example.foodie

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class FoodItem(
    val itemName: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val category: String = "",
    val imageName: String = "", // Corrected from imageUrl
    val ingredients: List<String> = emptyList()
) : Parcelable
