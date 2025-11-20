package com.example.foodie

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var foodItemAdapter: FoodItemAdapter
    private lateinit var searchEditText: EditText
    private lateinit var searchIcon: ImageView
    private lateinit var btnAll: Button
    private lateinit var btnBurger: Button
    private lateinit var btnCombo: Button
    private lateinit var btnFries: Button
    private lateinit var btnDrinks: Button
    private var currentSelectedButton: Button? = null
    private lateinit var cartBtn: ImageButton
    private lateinit var profileBtn: ImageButton
    private lateinit var trackOrderBtn: ImageButton
    private lateinit var orderHistoryBtn: ImageButton
    private lateinit var imgBtnChat: ImageButton

    private val allItems = mutableListOf<FoodItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize views
        initializeViews()

        // Initialize RecyclerView and set up the Adapter
        setupRecyclerView()

        // Set up the buttons for filtering
        setupCategoryButtons()

        // Set up search functionality
        setupSearchView()

        cartBtn.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        profileBtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        trackOrderBtn.setOnClickListener {
            val intent = Intent(this, TrackOrderActivity::class.java)
            startActivity(intent)
        }

        orderHistoryBtn.setOnClickListener {
            val intent = Intent(this, OrderHistoryActivity::class.java)
            startActivity(intent)
        }

        imgBtnChat.setOnClickListener {
            val intent = Intent(this, ChatbotActivity::class.java)
            startActivity(intent)
        }

        fetchFoodItems()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        searchIcon = findViewById(R.id.searchIcon)
        btnAll = findViewById(R.id.btnAll)
        btnBurger = findViewById(R.id.btnBurger)
        btnCombo = findViewById(R.id.btnCombo)
        btnFries = findViewById(R.id.btnFries)
        btnDrinks = findViewById(R.id.btnDrinks)
        cartBtn = findViewById(R.id.cartbtn)
        profileBtn = findViewById(R.id.profilebtn)
        trackOrderBtn = findViewById(R.id.trackOrderBtn)
        orderHistoryBtn = findViewById(R.id.imageButton5)
        imgBtnChat = findViewById(R.id.imgBtnChat)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        foodItemAdapter = FoodItemAdapter(this, allItems)
        recyclerView.adapter = foodItemAdapter
    }

    private fun setupCategoryButtons() {
        // Set initial selected button
        currentSelectedButton = btnAll
        updateButtonAppearance(btnAll, true)

        btnAll.setOnClickListener {
            selectButton(btnAll)
            filterItems("All")
        }

        btnBurger.setOnClickListener {
            selectButton(btnBurger)
            filterItems("Burger")
        }

        btnCombo.setOnClickListener {
            selectButton(btnCombo)
            filterItems("Combo")
        }

        btnFries.setOnClickListener {
            selectButton(btnFries)
            filterItems("Fries")
        }

        btnDrinks.setOnClickListener {
            selectButton(btnDrinks)
            filterItems("Drinks")
        }
    }

    private fun setupSearchView() {
        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: android.text.Editable?) {
                val newText = s.toString()
                if (newText.isEmpty()) {
                    val category = when (currentSelectedButton) {
                        btnBurger -> "Burger"
                        btnCombo -> "Combo"
                        btnFries -> "Fries"
                        btnDrinks -> "Drinks"
                        else -> "All"
                    }
                    filterItems(category)
                } else {
                    val filteredList = allItems.filter { item ->
                        item.itemName.contains(newText, ignoreCase = true) ||
                                item.description.contains(newText, ignoreCase = true)
                    }
                    foodItemAdapter.updateItems(filteredList)
                }
            }
        })
    }

    private fun selectButton(selectedButton: Button) {
        currentSelectedButton?.let { updateButtonAppearance(it, false) }
        currentSelectedButton = selectedButton
        updateButtonAppearance(selectedButton, true)
    }

    private fun updateButtonAppearance(button: Button, isSelected: Boolean) {
        if (isSelected) {
            button.setBackgroundResource(R.drawable.button_selected)
            button.setTextColor(resources.getColor(R.color.white, null))
        } else {
            button.setBackgroundResource(R.drawable.button_unselected)
            button.setTextColor(resources.getColor(R.color.gray, null))
        }
    }

    private fun filterItems(category: String) {
        val categoryToFilter = if (category.equals("Drinks", ignoreCase = true)) "drink" else category
        val filteredList = if (categoryToFilter == "All") {
            allItems
        } else {
            allItems.filter { it.category.equals(categoryToFilter, ignoreCase = true) }
        }
        foodItemAdapter.updateItems(filteredList)
    }

    private fun fetchFoodItems() {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("menu_items").get()
            .addOnSuccessListener { documents ->
                allItems.clear() // Clear the list before adding new items
                for (document in documents) {
                    val foodItem = document.toObject(FoodItem::class.java)
                    // Here, you would ideally get a drawable resource ID based on the imageName
                    // For now, let's assume you have a way to map imageName to a resource ID
                    // or you are loading from a URL. For this example, we'll just add the item.
                    allItems.add(foodItem)
                }
                foodItemAdapter.updateItems(allItems)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting documents: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
