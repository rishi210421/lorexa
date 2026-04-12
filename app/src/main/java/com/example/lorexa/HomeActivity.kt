package com.example.lorexa

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CharacterAdapter

    private lateinit var btnAll: Button
    private lateinit var btnScientists: Button
    private lateinit var btnPhilosophers: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_home)

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
val homeBtn = findViewById<ImageView>(R.id.homeBtn)
        val chatBtn = findViewById<ImageView>(R.id.chatBtn)
        val navProfile = findViewById<ImageView>(R.id.profileBtn)
        val navSettings = findViewById<ImageView>(R.id.settingsBtn)

        navProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        // 🔥 CONNECT XML
        recyclerView = findViewById(R.id.recyclerView)
        val btnAll = findViewById<TextView>(R.id.btnAll)
        val btnScientists = findViewById<TextView>(R.id.btnScientists)
        val btnPhilosophers = findViewById<TextView>(R.id.btnPhilosophers)

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
        adapter.onItemClick = { character ->

            val intent = Intent(this, ChatListActivity::class.java)

            intent.putExtra("character", character.name)

            startActivity(intent)
        }
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

        chatBtn.setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }

//        navProfile.setOnClickListener {
//            val intent = Intent(this, ProfileActivity::class.java)
//            startActivity(intent)
//        }


        navSettings.setOnClickListener {
            Toast.makeText(this, "Settings Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }
}