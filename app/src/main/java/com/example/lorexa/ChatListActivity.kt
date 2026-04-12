package com.example.lorexa

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatListActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var recycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        recycler = findViewById(R.id.chatListRecycler)

        loadChats()
        findViewById<ImageView>(R.id.homeBtn).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        findViewById<ImageView>(R.id.chatBtn).setOnClickListener {
            // Already on chat list
        }

        findViewById<ImageView>(R.id.profileBtn).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<ImageView>(R.id.settingsBtn).setOnClickListener {
            Toast.makeText(this, "Settings Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadChats() {

        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING) // 🔥 IMPORTANT
            .get()
            .addOnSuccessListener { result ->

                val list = mutableListOf<ChatPreview>()

                for (doc in result) {

                    val name = doc.getString("name") ?: ""
                    val lastMsg = doc.getString("lastMessage") ?: ""
                    val time = doc.getLong("timestamp") ?: 0

                    list.add(ChatPreview(name, lastMsg, time))
                }

                recycler.layoutManager = LinearLayoutManager(this)
                recycler.adapter = ChatListAdapter(list) {

                    // 🔥 OPEN CHAT
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.putExtra("character", it.name)
                    startActivity(intent)
                }
            }
    }
}