package com.example.lorexa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CharacterAdapter

    private lateinit var btnAll: Button
    private lateinit var btnScientists: Button
    private lateinit var btnPhilosophers: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val navChat = findViewById<ImageView>(R.id.navChat)
        val navProfile = findViewById<ImageView>(R.id.navProfile)
        val navSettings = findViewById<ImageView>(R.id.navSettings)
        // 🔥 CONNECT XML
        recyclerView = findViewById(R.id.recyclerView)
        btnAll = findViewById(R.id.btnAll)
        btnScientists = findViewById(R.id.btnScientists)
        btnPhilosophers = findViewById(R.id.btnPhilosophers)

        // 🔥 FULL LIST
        val fullList = listOf(
            Character("Leonardo da Vinci", R.drawable.leonardo),
            Character("Cleopatra", R.drawable.cleopatra),
            Character("Martin Luther", R.drawable.martin),
            Character("Jane Austen", R.drawable.jane),
            Character("Albert Einstein", R.drawable.einstein),
            Character("Marie Curie", R.drawable.curie)
        )

        // 🔥 CURRENT LIST (THIS WILL CHANGE)
        val currentList = fullList.toMutableList()

        adapter = CharacterAdapter(currentList)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter

        adapter.onItemClick = { character ->

            val intent = Intent(this, CharacterDetailActivity::class.java)

            intent.putExtra("name", character.name)
            intent.putExtra("image", character.image)

            startActivity(intent)
        }

        // =========================
        // 🔥 BUTTON LOGIC
        // =========================

        // ALL
        btnAll.setOnClickListener {
            currentList.clear()
            currentList.addAll(fullList)
            adapter.notifyDataSetChanged()
        }

        // SCIENTISTS
        btnScientists.setOnClickListener {
            currentList.clear()
            currentList.addAll(fullList.filter {
                it.name.contains("Einstein") || it.name.contains("Curie")
            })
            adapter.notifyDataSetChanged()
        }

        // PHILOSOPHERS
        btnPhilosophers.setOnClickListener {
            currentList.clear()
            currentList.addAll(fullList.filter {
                it.name.contains("Martin")
            })
            adapter.notifyDataSetChanged()
        }

        navChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        navProfile.setOnClickListener {
            Toast.makeText(this, "Profile Coming Soon", Toast.LENGTH_SHORT).show()
        }

        navSettings.setOnClickListener {
            Toast.makeText(this, "Settings Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }
}