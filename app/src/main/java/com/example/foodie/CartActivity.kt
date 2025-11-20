package com.example.foodie

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartActivity : AppCompatActivity() {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var totalPriceTextView: TextView
    private lateinit var checkoutButton: Button
    private lateinit var cartAdapter: CartAdapter

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        cartRecyclerView = findViewById(R.id.cartRecyclerView)
        totalPriceTextView = findViewById(R.id.totalPriceTextView)
        checkoutButton = findViewById(R.id.checkoutButton)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(CartRepository.items) { updateTotalPrice() }
        cartRecyclerView.adapter = cartAdapter

        updateTotalPrice()

        checkoutButton.setOnClickListener {
            if (CartRepository.items.isNotEmpty()) {
                placeOrder()
            } else {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun placeOrder() {
        val userId = auth.currentUser?.uid ?: return
        val userDocRef = firestore.collection("user_detail").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            val address = document.getString("address")
            if (address.isNullOrEmpty()) {
                Toast.makeText(this, "Please add an address to your profile before checking out.", Toast.LENGTH_LONG).show()
                return@addOnSuccessListener
            }

            val orderId = firestore.collection("orders").document().id
            val order = Order(
                orderId = orderId,
                items = CartRepository.items,
                totalAmount = CartRepository.getCartTotal(),
                address = address,
                orderStatus = "Waiting to be accepted",
                userId = userId
            )

            firestore.collection("orders").document(orderId).set(order)
                .addOnSuccessListener {
                    Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()
                    CartRepository.clearCart()
                    cartAdapter.notifyDataSetChanged()
                    updateTotalPrice()
                    val intent = Intent(this, TrackOrderActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener { 
                    Toast.makeText(this, "Failed to place order.", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to retrieve user address.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        cartAdapter.notifyDataSetChanged()
        updateTotalPrice()
    }

    private fun updateTotalPrice() {
        totalPriceTextView.text = String.format("$%.2f", CartRepository.getCartTotal())
    }
}