package com.example.foodie

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MessageAdapter(private val messages: List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return messages[position].type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Message.TYPE_USER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_user, parent, false)
                UserMessageViewHolder(view)
            }
            Message.TYPE_BOT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_bot, parent, false)
                BotMessageViewHolder(view)
            }
            Message.TYPE_COMBO -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_combo, parent, false)
                ComboMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is BotMessageViewHolder -> holder.bind(message)
            is ComboMessageViewHolder -> {
                message.combo?.let { holder.bind(it) }
            }
        }
    }

    override fun getItemCount(): Int = messages.size

    inner class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageTextView)
        fun bind(message: Message) {
            messageText.text = message.text
        }
    }

    inner class BotMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageTextView)
        fun bind(message: Message) {
            messageText.text = message.text
        }
    }

    inner class ComboMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val introTextView: TextView = itemView.findViewById(R.id.comboIntroTextView)
        private val itemsLayout: LinearLayout = itemView.findViewById(R.id.comboItemsLayout)
        private val priceTextView: TextView = itemView.findViewById(R.id.comboPriceTextView)
        private val addToCartButton: Button = itemView.findViewById(R.id.addToCartButton)

        fun bind(combo: Combo) {
            introTextView.text = combo.introText
            priceTextView.text = String.format("Total: $%.2f", combo.totalPrice)

            itemsLayout.removeAllViews()
            combo.items.forEach { foodItem ->
                val itemTextView = TextView(itemsLayout.context).apply {
                    text = "- ${foodItem.itemName}"
                    textSize = 14f
                }
                itemsLayout.addView(itemTextView)
            }

            addToCartButton.setOnClickListener { view ->
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId == null) {
                    addItemsToCart(combo)
                    return@setOnClickListener
                }

                val db = FirebaseFirestore.getInstance()
                
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        val userAllergies = (document.get("allergies") as? List<String>)?.map { it.lowercase() } ?: emptyList()
                        
                        val conflictingItems = combo.items.filter { item ->
                            item.ingredients.any { ingredient -> 
                                userAllergies.contains(ingredient.lowercase()) 
                            }
                        }

                        if (conflictingItems.isNotEmpty()) {
                            val conflicts = conflictingItems.joinToString(", ") { it.itemName }
                            AlertDialog.Builder(view.context)
                                .setTitle("Allergy Warning")
                                .setMessage("Warning: The following items contain ingredients you are allergic to: $conflicts. Do you still want to add them?")
                                .setPositiveButton("Yes, Add Anyway") { _, _ ->
                                    addItemsToCart(combo)
                                }
                                .setNegativeButton("Cancel", null)
                                .show()
                        } else {
                            addItemsToCart(combo)
                        }
                    }
                    .addOnFailureListener {
                        addItemsToCart(combo) 
                    }
            }
        }

        private fun addItemsToCart(combo: Combo) {
            combo.items.forEach { foodItem ->
                val cartItem = CartItem(
                    itemName = foodItem.itemName,
                    itemPrice = foodItem.price,
                    quantity = 1, // Each item in a combo is added with quantity 1
                    itemImage = foodItem.imageName
                )
                CartRepository.addItem(cartItem)
            }
            Toast.makeText(itemView.context, "Items added to cart", Toast.LENGTH_SHORT).show()
        }
    }
}
