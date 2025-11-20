package com.example.foodie

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.io.InputStream

class ProductDetailsActivity : AppCompatActivity() {

    private var portionCount = 1
    private var basePrice = 0.0
    private var imageName: String? = null // To store the image name

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        val productName = intent.getStringExtra("itemName")
        val productDescription = intent.getStringExtra("description")
        imageName = intent.getStringExtra("imageName")
        basePrice = intent.getDoubleExtra("price", 0.0)

        val productNameTextView = findViewById<TextView>(R.id.productNameTextView)
        val productDescriptionTextView = findViewById<TextView>(R.id.productDescriptionTextView)
        val productImageView = findViewById<ImageView>(R.id.productImageView)
        val productCostTextView = findViewById<TextView>(R.id.productCostView)

        productNameTextView.text = productName
        productDescriptionTextView.text = productDescription
        updatePrice(productCostTextView)

        try {
            imageName?.let {
                val inputStream: InputStream = assets.open(it)
                val drawable = Drawable.createFromStream(inputStream, null)
                productImageView.setImageDrawable(drawable)
            }
        } catch (e: IOException) {
            Log.e("ProductDetailsActivity", "Error loading image: ${e.message}")
            productImageView.setImageResource(R.drawable.default_food_image)
        }

        val decreasePortionButton = findViewById<Button>(R.id.decreasePortionButton)
        val increasePortionButton = findViewById<Button>(R.id.increasePortionButton)
        val portionCountTextView = findViewById<TextView>(R.id.portionCountTextView)

        decreasePortionButton.setOnClickListener {
            if (portionCount > 1) {
                portionCount--
                portionCountTextView.text = portionCount.toString()
                updatePrice(productCostTextView)
            }
        }

        increasePortionButton.setOnClickListener {
            portionCount++
            portionCountTextView.text = portionCount.toString()
            updatePrice(productCostTextView)
        }

        val addToCartButton = findViewById<Button>(R.id.addToCartButton)
        addToCartButton.setOnClickListener {
            imageName?.let {
                val cartItem = CartItem(productName!!, basePrice, portionCount, it)
                CartRepository.addItem(cartItem)
                Toast.makeText(this, "${portionCount} ${productName}(s) added to cart", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updatePrice(productCostTextView: TextView) {
        val totalPrice = portionCount * basePrice
        productCostTextView.text = "$ ${String.format("%.2f", totalPrice)}"
    }
}