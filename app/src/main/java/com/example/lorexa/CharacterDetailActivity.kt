package com.example.lorexa

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CharacterDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_character_detail)
        val root = findViewById<View>(R.id.rootLayout)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->

            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
        val characterName = intent.getStringExtra("name") ?: "Unknown"
        val image = intent.getIntExtra("image", 0)

        val favBtn = findViewById<Button>(R.id.favBtn)

        favBtn.setOnClickListener {

            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = user.uid

            val data = hashMapOf(
                "name" to characterName,
                "timestamp" to System.currentTimeMillis()
            )

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("favourites")
                .document(characterName)
                .set(data)

            Toast.makeText(this, "Added to Favorites ❤️", Toast.LENGTH_SHORT).show()
        }
//        findViewById<TextView>(R.id.characterName).text = name
        findViewById<ImageView>(R.id.characterImage).setImageResource(image)

//        val characterName = intent.getStringExtra("name")

        findViewById<Button>(R.id.startChatBtn).setOnClickListener {

            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("character", characterName)

            startActivity(intent)
        }

        findViewById<ImageView>(R.id.backBtn).setOnClickListener {
            finish()
        }
    }
}