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
            // The imageName from Firebase already contains the full path (e.g., "burger/chicken_burger.jpg")
            val imagePath = item.imageName
            Log.d("FoodItemAdapter", "Loading image from assets: $imagePath") // Logging the path
            val inputStream: InputStream = context.assets.open(imagePath)
            val drawable = Drawable.createFromStream(inputStream, null)
            holder.itemImage.setImageDrawable(drawable)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("FoodItemAdapter", "Error loading image '$' from assets: ${'$'}{e.message}", e) // Logging the error
            // Optionally, set a placeholder image in case of an error
            holder.itemImage.setImageResource(R.drawable.default_food_image)
        }

        // Set the click listener for the CardView to navigate to the ProductDetailsActivity
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProductDetailsActivity::class.java).apply {
                putExtra("itemName", item.itemName)
                putExtra("price", item.price)
                putExtra("category", item.category)
                putExtra("imageName", item.imageName)
                putExtra("description", item.description)
                putStringArrayListExtra("ingredients", ArrayList(item.ingredients))
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
