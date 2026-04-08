package com.example.lorexa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CharacterDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character_detail)

        val name = intent.getStringExtra("name")
        val image = intent.getIntExtra("image", 0)

        findViewById<TextView>(R.id.characterName).text = name
        findViewById<ImageView>(R.id.characterImage).setImageResource(image)

        val characterName = intent.getStringExtra("name")

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