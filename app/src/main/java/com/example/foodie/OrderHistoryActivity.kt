package com.example.foodie

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var orderHistoryRecyclerView: RecyclerView
    private lateinit var orderHistoryAdapter: OrderHistoryAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        orderHistoryRecyclerView = findViewById(R.id.orderHistoryRecyclerView)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadOrderHistory()
    }

    private fun setupRecyclerView() {
        orderHistoryAdapter = OrderHistoryAdapter(emptyList()) { orderId ->
            deleteOrder(orderId)
        }
        orderHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        orderHistoryRecyclerView.adapter = orderHistoryAdapter
    }

    private fun loadOrderHistory() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("orderId", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val orders = documents.toObjects(Order::class.java)
                orderHistoryAdapter.updateOrders(orders)
            }
            .addOnFailureListener { 
                Toast.makeText(this, "Failed to load order history.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteOrder(orderId: String) {
        firestore.collection("orders").document(orderId).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Order deleted successfully.", Toast.LENGTH_SHORT).show()
                loadOrderHistory() // Refresh the list
            }
            .addOnFailureListener { 
                Toast.makeText(this, "Failed to delete order.", Toast.LENGTH_SHORT).show()
            }
    }
}
