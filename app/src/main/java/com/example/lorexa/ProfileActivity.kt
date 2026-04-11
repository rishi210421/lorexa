package com.example.lorexa

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ProfileActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val root = findViewById<View>(android.R.id.content)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->

            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                systemBars.left,
                systemBars.top,   // 🔥 THIS FIXES TOP CUT
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
        loadFavorites()
        loadAchievements()
        loadMostContacted()
        val userNameText = findViewById<TextView>(R.id.userName)
        val favCount = findViewById<TextView>(R.id.favCount)
        val conversationCount = findViewById<TextView>(R.id.conversationCount)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "User"
                userNameText.text = name
            }

         // 🔥 LOAD FAVORITES COUNT
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("favourites")
            .get()
            .addOnSuccessListener {
                favCount.text = it.size().toString()
            }

           // 🔥 LOAD CONVERSATION COUNT
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("messages")
            .get()
            .addOnSuccessListener { result ->

                var totalMessages = 0
                var completed = 0

                if (result.isEmpty) {
                    conversationCount.text = "0"
                    return@addOnSuccessListener
                }

                for (doc in result) {

                    doc.reference.collection("chat")
                        .get()
                        .addOnSuccessListener { chatResult ->

                            totalMessages += chatResult.size()
                            completed++

                            if (completed == result.size()) {
                                conversationCount.text = totalMessages.toString()
                            }
                        }
                }
            }

        val achievementContainer = findViewById<LinearLayout>(R.id.achievementContainer)

        fun addAchievement(title: String, desc: String) {

            val card = LinearLayout(this)
            card.orientation = LinearLayout.VERTICAL
            card.setPadding(24,24,24,24)
            card.setBackgroundResource(R.drawable.card_bg)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16)   // 🔥 spacing fix
            card.layoutParams = params

            val t = TextView(this)
            t.text = title
            t.setTextColor(resources.getColor(android.R.color.white))
            t.textSize = 16f

            val d = TextView(this)
            d.text = desc
            d.setTextColor(resources.getColor(android.R.color.darker_gray))

            card.addView(t)
            card.addView(d)

            achievementContainer.addView(card)
        }
         // Example achievements
        addAchievement("Time Traveler", "Chat with 5 figures")
        addAchievement("Renaissance Mind", "Explore all")
    }

    private fun loadAchievements() {

        val userId = auth.currentUser?.uid ?: return
        val container = findViewById<LinearLayout>(R.id.achievementContainer)

        db.collection("users")
            .document(userId)
            .collection("achievements")
            .get()
            .addOnSuccessListener {

                for (doc in it) {

                    val title = doc.getString("title") ?: ""

                    val tv = TextView(this)
                    tv.text = "🏆 $title"
                    tv.setTextColor(Color.YELLOW)

                    container.addView(tv)
                }
            }
    }

    private fun loadFavorites() {

        val userId = auth.currentUser?.uid ?: return
        val container = findViewById<LinearLayout>(R.id.favContainer)

        db.collection("users")
            .document(userId)
            .collection("favourites")
            .get()
            .addOnSuccessListener {

                for (doc in it) {

                    val character = doc.getString("name") ?: ""

                    val card = LinearLayout(this)
                    card.orientation = LinearLayout.HORIZONTAL
                    card.setPadding(20,20,20,20)
                    card.setBackgroundResource(R.drawable.card_bg)

                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 0, 0, 12)
                    card.layoutParams = params

                    val name = TextView(this)
                    name.text = character
                    name.setTextColor(resources.getColor(android.R.color.white))
                    name.textSize = 16f

                    val chatCount = TextView(this)
                    chatCount.text = "🔥 Favorite"
                    chatCount.setTextColor(resources.getColor(android.R.color.holo_orange_light))
                    chatCount.textSize = 14f

                    // spacing between texts
                    chatCount.setPadding(20, 0, 0, 0)
                    val layout = LinearLayout(this)
                    layout.orientation = LinearLayout.HORIZONTAL
                    layout.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                    name.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                    layout.addView(name)
                    layout.addView(chatCount)

                    card.addView(layout)
                    container.addView(card)
                }
            }
    }

    private fun loadMostContacted() {

        val userId = auth.currentUser?.uid ?: return
        val container = findViewById<LinearLayout>(R.id.mostContactedContainer)

        db.collection("users")
            .document(userId)
            .collection("messages")
            .get()
            .addOnSuccessListener { result ->

                val list = mutableListOf<Pair<String, Int>>()
                var completed = 0
                val total = result.size()

                if (total == 0) return@addOnSuccessListener

                for (doc in result) {

                    val characterName = doc.id

                    // ❌ skip random ids
                    if (characterName.length > 25) {
                        completed++
                        continue
                    }

                    doc.reference.collection("chat")
                        .get()
                        .addOnSuccessListener { chatResult ->

                            val count = chatResult.size()
                            list.add(Pair(characterName, count))

                            completed++

                            // ✅ wait until ALL finished
                            if (completed == total) {

                                val sorted = list
                                    .filter { it.second > 0 }   // 🔥 remove 0 chats
                                    .sortedByDescending { it.second }
                                    .take(3)

                                container.removeAllViews()

                                for ((name, count) in sorted) {
                                    addMostContactedCard(container, name, count)
                                }
                            }
                        }
                }
                Log.d("FIREBASE_LOAD", "Loading Most Contacted...")            }

    }
    private fun addMostContactedCard(container: LinearLayout, character: String, count: Int) {

        val card = LinearLayout(this)
        card.orientation = LinearLayout.HORIZONTAL
        card.setPadding(24,24,24,24)
        card.setBackgroundResource(R.drawable.card_bg)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 16)
        card.layoutParams = params

        val name = TextView(this)
        name.text = character
        name.setTextColor(resources.getColor(android.R.color.white))
        name.textSize = 16f

        val chatCount = TextView(this)
        chatCount.text = "$count chats"
        chatCount.setTextColor(resources.getColor(android.R.color.holo_orange_light))
        chatCount.textSize = 14f

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.HORIZONTAL
        layout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        name.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        layout.addView(name)
        layout.addView(chatCount)

        card.addView(layout)

        container.addView(card)
    }

}