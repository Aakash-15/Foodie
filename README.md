🍔 Foodie – AI-Powered Food Ordering App

Foodie is a Kotlin-based Android application designed for a smooth and interactive food-ordering experience. The app focuses on burgers, fries, and cold drinks, and integrates real-time user interactions through Google’s Gemini API for an intelligent in-app food assistant. Foodie features a secure login system using email + password, a structured signup flow (email, password, contact, address), and dynamic content pulled from a Realtime Database. The AI agent is highly restricted to the database content and can warn users about allergens (e.g., peri-peri), recommend alternatives, and assist with cart decisions—even for items added through normal UI buttons. The app includes rich visuals with multiple burger images on the Home Screen and is built with a modular architecture for scalability. No Jetpack Compose is used; the UI is entirely XML-based.

Tech Stack

Language: Kotlin

Database: Firebase Realtime Database

AI: Gemini API (Chatbot + Intent-based recommendations)

UI: XML layouts

Authentication: Firebase Auth (Email/Password)

Architecture: MVVM-inspired modular flow

Features: AI-assisted ordering, allergen detection, real-time chat, image-rich home screen, cart & menu management
