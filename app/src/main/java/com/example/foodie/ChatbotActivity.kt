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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChatbotActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<Message>()

    private val db = FirebaseFirestore.getInstance()
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash-lite",
        apiKey = "YOUR_API_KEY"
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
                addMessage(Message(userMessage, Message.TYPE_USER))
                messageEditText.text.clear()
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
                // Safely fetch and parse menu items from Firestore
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
                            imageName = document.getString("imageName") ?: ""
                        )
                        menuItems.add(item)
                    } catch (e: Exception) {
                        Log.w("ChatbotActivity", "Failed to parse menu item: ${document.id}", e)
                    }
                }

                val menuData = menuItems.joinToString("\n") { item ->
                    "- ${item.itemName} (Price: ${item.price}, Category: ${item.category}, Description: ${item.description})"
                }

                val prompt = """You are a food assistant for an app named Foodie.
                Restrict your responses to only food-related topics. If asked about anything else, politely refuse.
                You have access to the restaurant's menu below. Use this information to answer the user's questions and provide helpful, relevant text responses.

                MENU:
                $menuData

                USER'S QUESTION: "$messageText"
                """

                val response = withContext(Dispatchers.IO) {
                    generativeModel.generateContent(prompt)
                }

                response.text?.let {
                    addMessage(Message(it, Message.TYPE_BOT))
                }

            } catch (e: Throwable) {
                addMessage(Message("Sorry, I couldn't process the menu right now.", Message.TYPE_BOT))
                Toast.makeText(this@ChatbotActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
