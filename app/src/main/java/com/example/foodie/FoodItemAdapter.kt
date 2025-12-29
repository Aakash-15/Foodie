package com.example.foodie

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.io.InputStream

class FoodItemAdapter(
    private val context: Context,
    private var itemList: List<FoodItem>
) : RecyclerView.Adapter<FoodItemAdapter.FoodItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_home, parent, false)
        return FoodItemViewHolder(view)
    }

    fun updateItems(newItems: List<FoodItem>) {
        itemList = newItems
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: FoodItemViewHolder, position: Int) {
        val item = itemList[position]

        holder.itemName.text = item.itemName
        holder.itemPrice.text = "$${String.format("%.2f", item.price)}"

        // Load image from assets
        try {
            val imagePath = item.imageName
            val inputStream: InputStream = context.assets.open(imagePath)
            val drawable = Drawable.createFromStream(inputStream, null)
            holder.itemImage.setImageDrawable(drawable)
        } catch (e: IOException) {
            Log.e("FoodItemAdapter", "Error loading image from assets: ${item.imageName}", e)
            holder.itemImage.setImageResource(R.drawable.default_food_image)
        }

        // Set the click listener for the CardView to navigate to the ProductDetailsActivity
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProductDetailsActivity::class.java).apply {
                putExtra("foodItem", item) // Pass the entire FoodItem object
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = itemList.size

    class FoodItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ImageView = view.findViewById(R.id.burgerImage)
        val itemName: TextView = view.findViewById(R.id.burgerName)
        val itemPrice: TextView = view.findViewById(R.id.burgerPrice) // You will need to add this to your item_home.xml
    }
}
