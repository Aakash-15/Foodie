package com.example.foodie

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ChatbotActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<Message>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Using the API Key you provided previously
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash-lite",
        apiKey = "AIzaSyDOtTGeEuv-JLLz7rO9AWgxSpdvdBUU2cw"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        setupUI()
        setupSendButton()
    }

    private fun setupUI() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        messageAdapter = MessageAdapter(messageList)
        chatRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        chatRecyclerView.adapter = messageAdapter
    }

    private fun setupSendButton() {
        sendButton.setOnClickListener {
            val userMessage = messageEditText.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                // 1. Add User Message to UI
                addMessage(Message(text = userMessage, type = Message.TYPE_USER))
                messageEditText.text.clear()

                // 2. Send to AI
                processUserMessage(userMessage)
            }
        }
    }

    private fun addMessage(message: Message) {
        runOnUiThread {
            messageList.add(message)
            messageAdapter.notifyItemInserted(messageList.size - 1)
            chatRecyclerView.scrollToPosition(messageList.size - 1)
        }
    }

    private fun processUserMessage(messageText: String) {
        lifecycleScope.launch {
            try {
                // --- A. Fetch Menu Data ---
                val menuItems = mutableListOf<FoodItem>()
                val snapshot = withContext(Dispatchers.IO) {
                    db.collection("menu_items").get().await()
                }

                for (document in snapshot.documents) {
                    try {
                        val item = FoodItem(
                            itemName = document.getString("itemName") ?: "",
                            price = document.getDouble("price") ?: 0.0,
                            description = document.getString("description") ?: "",
                            category = document.getString("category") ?: "",
                            imageName = document.getString("imageName") ?: "",
                            ingredients = (document.get("ingredients") as? List<String>) ?: emptyList()
                        )
                        menuItems.add(item)
                    } catch (e: Exception) {
                        Log.w("ChatbotActivity", "Failed to parse menu item: ${document.id}", e)
                    }
                }

                val menuData = menuItems.joinToString("\n") { item ->
                    "- ${item.itemName} ($${item.price})"
                }

                // --- B. Build Robust History (CRITICAL FIX) ---
                // This ensures the AI remembers previous Combos correctly
                val history = messageList.takeLast(8).joinToString("\n") { msg ->
                    val sender = if (msg.type == Message.TYPE_USER) "User" else "Bot"

                    val content = if (msg.type == Message.TYPE_COMBO && msg.combo != null) {
                        // Describe the combo textually so the AI understands its own history
                        val items = msg.combo.items.joinToString(", ") { it.itemName }
                        "Suggested a combo: $items for $${msg.combo.totalPrice}"
                    } else {
                        msg.text ?: ""
                    }
                    "$sender: $content"
                }

                // --- C. Create Prompt ---
                val prompt = """You are Foodie, a helpful food assistant.
                
                **Conversation History:**
                $history

                **Strict Rules:**
                1. If the user asks for a combo/meal, return a JSON object: `{"isComboSuggestion": true, "introText": "...", "items": [{"itemName": "Burger"}], "totalPrice": 15.0}`.
                2. If the user updates an allergy, return JSON: `{"isAllergyUpdate": true, "allergy": "peanuts"}`.
                3. Otherwise, respond in plain text.

                **Menu:**
                $menuData

                **User Input:** "$messageText"
                """

                val response = withContext(Dispatchers.IO) {
                    generativeModel.generateContent(prompt)
                }

                response.text?.let {
                    handleBotResponse(it, menuItems)
                }

            } catch (e: java.lang.SecurityException) {
                // Specific handling for the "Unknown calling package" error
                Log.e("ChatbotActivity", "Security Exception", e)
                addMessage(Message(text = "System Error: Please reinstall the app or restart the emulator.", type = Message.TYPE_BOT))
            } catch (e: Throwable) {
                Log.e("ChatbotActivity", "General Error", e)
                addMessage(Message(text = "I'm having trouble connecting right now.", type = Message.TYPE_BOT))
            }
        }
    }

    private fun handleBotResponse(responseText: String, menuItems: List<FoodItem>) {
        // --- D. Extract JSON Safely ---
        val jsonString = extractJson(responseText)

        if (jsonString != null) {
            try {
                val json = JSONObject(jsonString)

                // Case 1: Allergy Update
                if (json.optBoolean("isAllergyUpdate")) {
                    val allergy = json.getString("allergy")
                    saveAllergyToProfile(allergy)
                    addMessage(Message(text = "Noted. I've added $allergy to your allergy profile.", type = Message.TYPE_BOT))
                    return
                }

                // Case 2: Combo Suggestion
                if (json.optBoolean("isComboSuggestion")) {
                    val intro = json.getString("introText")
                    val itemsArray = json.getJSONArray("items")
                    val total = json.getDouble("totalPrice")
                    val comboItems = mutableListOf<FoodItem>()

                    for (i in 0 until itemsArray.length()) {
                        val name = itemsArray.getJSONObject(i).getString("itemName")
                        // Find matching item in menu list (Case insensitive)
                        val match = menuItems.find { it.itemName.equals(name, ignoreCase = true) }
                        if (match != null) comboItems.add(match)
                    }

                    if (comboItems.isNotEmpty()) {
                        addMessage(Message(combo = Combo(intro, comboItems, total), type = Message.TYPE_COMBO))
                    } else {
                        addMessage(Message(text = "I found a combo, but couldn't match the items to the menu.", type = Message.TYPE_BOT))
                    }
                    return
                }
            } catch (e: Exception) {
                Log.e("ChatbotActivity", "JSON Parse Error", e)
                // Fallback to text if JSON fails
            }
        }

        // Case 3: Plain Text (Clean up any leftover markdown)
        val cleanText = responseText.replace("```json", "").replace("```", "").trim()
        addMessage(Message(text = cleanText, type = Message.TYPE_BOT))
    }

    // Helper to find JSON inside a string
    private fun extractJson(text: String): String? {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        return if (start != -1 && end != -1 && end > start) {
            text.substring(start, end + 1)
        } else {
            null
        }
    }

    private fun saveAllergyToProfile(allergy: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .update("allergies", FieldValue.arrayUnion(allergy.lowercase()))
            .addOnFailureListener {
                db.collection("users").document(userId)
                    .set(mapOf("allergies" to listOf(allergy.lowercase())), SetOptions.merge())
            }
    }
}