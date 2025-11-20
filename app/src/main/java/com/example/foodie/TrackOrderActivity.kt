package com.example.foodie

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TrackOrderActivity : AppCompatActivity() {

    private lateinit var statusTextView: TextView
    private lateinit var totalBillTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var orderItemsRecyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var currentOrderId: String? = null
    private val TAG = "TrackOrderActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_order)

        statusTextView = findViewById(R.id.statusTextView)
        totalBillTextView = findViewById(R.id.totalBillTextView)
        addressTextView = findViewById(R.id.addressTextView)
        orderItemsRecyclerView = findViewById(R.id.orderItemsRecyclerView)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        orderItemsRecyclerView.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(mutableListOf()) {}
        cartAdapter.isQuantityChangeable = false
        orderItemsRecyclerView.adapter = cartAdapter

        loadOrderDetails()
    }

    private fun loadOrderDetails() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("orderId", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed.", e)
                    Toast.makeText(this, "Error loading order details: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    try {
                        val order = snapshots.documents[0].toObject(Order::class.java)
                        currentOrderId = snapshots.documents[0].id
                        val finalStatuses = listOf("delivered", "completed")

                        if (order != null && order.orderStatus.toLowerCase() in finalStatuses.map { it.toLowerCase() }) {
                            showNoActiveOrders()
                        } else {
                            order?.let {
                                Log.d(TAG, "Updating UI with order status: ${it.orderStatus}")
                                statusTextView.text = it.orderStatus
                                totalBillTextView.text = String.format("$%.2f", it.totalAmount)
                                addressTextView.text = "Address: ${it.address}"
                                cartAdapter.updateCartItems(it.items)
                            }
                        }
                    } catch (ex: Exception) {
                        Log.e(TAG, "Error deserializing order", ex)
                        Toast.makeText(this, "An error occurred: ${ex.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    showNoActiveOrders()
                }
            }
    }

    private fun showNoActiveOrders() {
        statusTextView.text = "No active orders"
        totalBillTextView.text = ""
        addressTextView.text = ""
        cartAdapter.updateCartItems(emptyList())
    }
}
