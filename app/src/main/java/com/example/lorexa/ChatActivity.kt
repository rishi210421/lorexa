package com.example.lorexa

import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class ChatActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var adapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.chatRecyclerView)

        adapter = ChatAdapter(messageList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val editText = findViewById<EditText>(R.id.editText)
        val sendButton = findViewById<Button>(R.id.sendButton)

        // ✅ OkHttp (timeout fix)
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        // ✅ Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://openrouter.ai/api/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        sendButton.setOnClickListener {
            val text = editText.text.toString()

            if (text.isNotBlank()) {

                // ✅ Add user message
                messageList.add(ChatMessage(text, true))
                adapter.notifyItemInserted(messageList.size - 1)
                recyclerView.scrollToPosition(messageList.size - 1)

                editText.text.clear()

                performChat(text)
            }
        }
    }

    private fun performChat(userInput: String) {
        lifecycleScope.launch {
            try {
                val messages = listOf(
                    Message(
                        role = "system",
                        content = """
                            You are Albert Einstein.

                            Speak in clear, simple English. 
                            Be thoughtful, curious, slightly philosophical, but NOT overly dramatic.

                            Avoid fake German words like "mein freund".
                            Keep responses natural, intelligent, and slightly witty.
                              talk like you are chatting with some person means keep reply ,imimal and only required information don't give very long reply or unuseful information .
                        """
                    ),
                    Message("user", userInput)
                )

                val request = ChatRequest(
                    model = "google/gemma-3-4b-it",
                    messages = messages
                )

                val token = "Bearer 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" // 🔒 keep your real key

                val response = apiService.sendMessage(
                    token,
                    "https://lorexa.app",
                    "Lorexa",
                    request
                )

                val aiText = response.choices.firstOrNull()?.message?.content
                    ?: "No AI reply received"

                // ✅ Add AI message
                messageList.add(ChatMessage(aiText, false))
                adapter.notifyItemInserted(messageList.size - 1)
                recyclerView.scrollToPosition(messageList.size - 1)

            } catch (e: Exception) {
                messageList.add(ChatMessage("Error: ${e.message}", false))
                adapter.notifyItemInserted(messageList.size - 1)
            }
        }
    }
}