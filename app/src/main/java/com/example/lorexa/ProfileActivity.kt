package com.example.lorexa

import android.graphics.Color
import android.os.Bundle
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
            .addOnSuccessListener {
                conversationCount.text = it.size().toString()
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
        addAchievement("Deep Thinker", "50+ messages")
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

                    val name = doc.getString("name") ?: ""

                    val tv = TextView(this)
                    tv.text = name
                    tv.setTextColor(Color.WHITE)

                    container.addView(tv)
                }
            }
    }

}