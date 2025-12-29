package com.example.foodie

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException
import java.io.InputStream

class ProductDetailsActivity : AppCompatActivity() {

    private var portionCount = 1
    private lateinit var foodItem: FoodItem

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        foodItem = intent.getParcelableExtra("foodItem") ?: return

        val productNameTextView = findViewById<TextView>(R.id.productNameTextView)
        val productDescriptionTextView = findViewById<TextView>(R.id.productDescriptionTextView)
        val productImageView = findViewById<ImageView>(R.id.productImageView)
        val productCostTextView = findViewById<TextView>(R.id.productCostView)

        productNameTextView.text = foodItem.itemName
        productDescriptionTextView.text = foodItem.description
        updatePrice(productCostTextView)

        try {
            val inputStream: InputStream = assets.open(foodItem.imageName)
            val drawable = Drawable.createFromStream(inputStream, null)
            productImageView.setImageDrawable(drawable)
        } catch (e: IOException) {
            Log.e("ProductDetailsActivity", "Error loading image: ${e.message}")
            productImageView.setImageResource(R.drawable.default_food_image) // A default image
        }

        setupPortionControls(productCostTextView)
        setupAddToCartButton()
        checkForAllergies()
    }

    private fun setupPortionControls(productCostTextView: TextView) {
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
    }

    private fun setupAddToCartButton() {
        val addToCartButton = findViewById<Button>(R.id.addToCartButton)
        addToCartButton.setOnClickListener {
            val cartItem = CartItem(foodItem.itemName, foodItem.price, portionCount, foodItem.imageName)
            CartRepository.addItem(cartItem)
            Toast.makeText(this, "${portionCount} ${foodItem.itemName}(s) added to cart", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkForAllergies() {
        val userId = auth.currentUser?.uid ?: return
        val allergyWarningTextView = findViewById<TextView>(R.id.allergyWarningTextView)

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val userAllergies = (document.get("allergies") as? List<String>)?.map { it.lowercase() } ?: emptyList()
                
                if (userAllergies.isEmpty()) return@addOnSuccessListener

                // Check if any ingredient contains any of the user's allergies
                val hasAllergy = foodItem.ingredients.any { ingredient ->
                    userAllergies.any { allergy ->
                        ingredient.lowercase().contains(allergy)
                    }
                }

                if (hasAllergy) {
                    allergyWarningTextView.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                Log.w("ProductDetailsActivity", "Error fetching user allergies", e)
            }
    }

    @SuppressLint("SetTextI18n")
    private fun updatePrice(productCostTextView: TextView) {
        val totalPrice = portionCount * foodItem.price
        productCostTextView.text = "$ ${String.format("%.2f", totalPrice)}"
    }
}
