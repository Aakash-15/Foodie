package com.example.foodie

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.io.InputStream

class CartAdapter(
    private val cartItems: MutableList<CartItem>,
    private val onQuantityChanged: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    var isQuantityChangeable: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]
        holder.bind(cartItem)
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateCartItems(newCartItems: List<CartItem>) {
        cartItems.clear()
        cartItems.addAll(newCartItems)
        notifyDataSetChanged()
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemName: TextView = itemView.findViewById(R.id.burgerName)
        private val itemPrice: TextView = itemView.findViewById(R.id.burgerPrice)
        private val itemImage: ImageView = itemView.findViewById(R.id.burgerImage)
        private val quantity: TextView = itemView.findViewById(R.id.quantityTextView)
        private val minusButton: ImageButton = itemView.findViewById(R.id.minusButton)
        private val plusButton: ImageButton = itemView.findViewById(R.id.plusButton)

        fun bind(cartItem: CartItem) {
            itemName.text = cartItem.itemName
            itemPrice.text = "$${cartItem.itemPrice}"
            quantity.text = cartItem.quantity.toString()

            if (!isQuantityChangeable) {
                minusButton.visibility = View.GONE
                plusButton.visibility = View.GONE
            } else {
                minusButton.visibility = View.VISIBLE
                plusButton.visibility = View.VISIBLE
            }

            try {
                val imagePath = cartItem.itemImage
                val inputStream: InputStream = itemView.context.assets.open(imagePath)
                val drawable = Drawable.createFromStream(inputStream, null)
                itemImage.setImageDrawable(drawable)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("CartAdapter", "Error loading image for ${cartItem.itemName}: ${e.message}")
                itemImage.setImageResource(R.drawable.default_food_image) // Fallback image
            }

            minusButton.setOnClickListener {
                if (cartItem.quantity > 1) {
                    cartItem.quantity--
                    quantity.text = cartItem.quantity.toString()
                    onQuantityChanged()
                } else {
                    cartItems.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)
                    onQuantityChanged()
                }
            }

            plusButton.setOnClickListener {
                cartItem.quantity++
                quantity.text = cartItem.quantity.toString()
                onQuantityChanged()
            }
        }
    }
}
