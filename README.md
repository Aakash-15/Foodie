# Foodie - AI-Powered Food Ordering App 🍔🤖

**Foodie** is a native Android application written in Kotlin that modernizes the food ordering experience. Beyond standard menu browsing, it features an intelligent **AI Concierge powered by Google Gemini**, allowing users to discover meals based on cravings, dietary restrictions, and budget through natural conversation.

---

## ✨ Key Features

### 🔐 Secure Authentication
* **Firebase Auth:** Secure Sign Up and Login using Email/Password.
* **Session Management:** "Remember Me" functionality using `SharedPreferences` and auto-login for returning users.
* **User Profile:** Stores delivery details and contact info.

### 📋 Dynamic Menu & Discovery
* **Live Data:** Real-time food listings fetched from **Firebase Firestore**.
* **Smart Search:** Client-side search bar for instant filtering by name or description.
* **Categorization:** Responsive `RecyclerView` grid for categories (Burgers, Combos, Drinks, etc.).

### 🤖 AI Food Assistant (Gemini Integration)
* **Context-Aware:** The chatbot is injected with the current menu data.
* **Dietary Smarts:** Automatically detects allergens (e.g., "Does this have nuts?") and suggests vegan/vegetarian options.
* **Natural Queries:** Users can ask, *"I'm in the mood for something spicy"* or *"What can I get for $10?"* and get accurate recommendations based **strictly** on available inventory.

### 🛒 Order Management
* **Rich Details:** Detailed product views with high-quality images, ingredient lists, and pricing.
* **Cart Logic:** (In Progress) Add to cart functionality via UI or Chat suggestions.

---

## 🛠 Tech Stack

| Category | Technology |
| :--- | :--- |
| **Language** | Kotlin |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **UI Toolkit** | XML Layouts, Material Design, ConstraintLayout |
| **Binding** | ViewBinding |
| **Backend** | Firebase (Auth, Firestore, Analytics) |
| **AI / LLM** | Google Generative AI SDK (Gemini Pro) |
| **Networking** | Coroutines & Flow |

---

## 🧠 How the AI Works (RAG-Lite)

The `ChatbotActivity` implements a lightweight **Retrieval-Augmented Generation (RAG)** approach to ensure the AI only sells what you have in stock.

1.  **Fetch:** Upon entering the chat, the app pulls the latest menu data from Firestore.
2.  **Context Injection:** The menu JSON/Objects are converted into a structured text prompt.
3.  **System Prompting:** We send a strict instruction to Gemini:
    > "You are a food assistant for 'Foodie'. Here is the current menu: [Menu Data]. Answer the user's question based ONLY on this menu. If an item is not listed, apologize and suggest an alternative."
4.  **Generation:** The AI processes the user query against this context and returns a natural language response.

---

## 📂 Project Structure

```text
com.example.foodie
├── activities
│   ├── LoginActivity.kt          # Auth & Session checks
│   ├── SignupActivity.kt         # Registration logic
│   ├── HomeActivity.kt           # Dashboard, Search, Category logic
│   ├── ChatbotActivity.kt        # Gemini integration & Prompt engineering
│   └── ProductDetailsActivity.kt # Individual item view
├── adapters
│   └── FoodItemAdapter.kt        # RecyclerView adapter for Menu Items
├── models
│   ├── FoodItem.kt               # Data class for Menu
│   └── User.kt                   # Data class for Profiles
└── utils
    └── Constants.kt              # API Keys & Config
```

## 🔮 Future Improvements

* **Payment Gateway:** Integration with Stripe or Razorpay.
* **Live Tracking:** Google Maps SDK integration for delivery tracking.
* **Push Notifications:** Firebase Cloud Messaging (FCM) for order status updates.
* **Offline Mode:** Room Database caching for viewing menus without internet.
