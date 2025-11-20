package com.example.foodie

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrderHistoryAdapter(
    private var orders: List<Order>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_history, parent, false)
        return OrderHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderHistoryViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    inner class OrderHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderIdTextView: TextView = itemView.findViewById(R.id.orderIdTextView)
        private val orderDateTextView: TextView = itemView.findViewById(R.id.orderDateTextView)
        private val orderStatusTextView: TextView = itemView.findViewById(R.id.orderStatusTextView)
        private val totalAmountTextView: TextView = itemView.findViewById(R.id.totalAmountTextView)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(order: Order) {
            orderIdTextView.text = "Order #${order.orderId?.take(6)}"
            orderDateTextView.text = "26-10-2025" // You can format a real date here
            orderStatusTextView.text = order.orderStatus
            totalAmountTextView.text = "Total: $${String.format("%.2f", order.totalAmount)}"

            deleteButton.setOnClickListener {
                order.orderId?.let { orderId ->
                    onDeleteClick(orderId)
                }
            }
        }
    }
}
