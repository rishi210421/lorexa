package com.example.lorexa


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CharacterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.recyclerView)

        val list = listOf(
            Character("Leonardo da Vinci", R.drawable.leonardo),
            Character("Cleopatra", R.drawable.cleopatra),
            Character("Martin Luther", R.drawable.martin),
            Character("Jane Austen", R.drawable.jane),
            Character("Albert Einstein", R.drawable.einstein),
            Character("Marie Curie", R.drawable.curie)
        )

        adapter = CharacterAdapter(list)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
    }
}