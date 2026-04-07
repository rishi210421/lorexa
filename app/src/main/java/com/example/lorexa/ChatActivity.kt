package com.example.lorexa

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

class ChatActivity : AppCompatActivity() {

    private var tts: TextToSpeech? = null
    private lateinit var apiService: ApiService
    private lateinit var adapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()
    private lateinit var recyclerView: RecyclerView

    // 🔥 FIREBASE
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // ✅ TTS INIT
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.ENGLISH
                tts?.setPitch(0.7f)
                tts?.setSpeechRate(0.85f)
            }
        }

        val micButton = findViewById<Button>(R.id.micButton)
        val editText = findViewById<EditText>(R.id.editText)
        val sendButton = findViewById<Button>(R.id.sendButton)

        recyclerView = findViewById(R.id.chatRecyclerView)
        adapter = ChatAdapter(messageList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // ✅ LOAD OLD CHAT FROM FIRESTORE
        loadMessages()

        // ✅ Retrofit
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://openrouter.ai/api/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        // ✅ SEND BUTTON
        sendButton.setOnClickListener {
            val text = editText.text.toString()
            if (text.isNotBlank()) {
                addMessage(text, true)
                saveMessage(text, "user") // 🔥 SAVE USER MESSAGE
                editText.text.clear()
                performChat(text)
            }
        }

        // 🎤 MIC
        micButton.setOnClickListener {
            startVoiceInput()
        }
    }

    // ✅ ADD MESSAGE TO UI
    private fun addMessage(text: String, isUser: Boolean) {
        messageList.add(ChatMessage(text, isUser))
        adapter.notifyItemInserted(messageList.size - 1)
        recyclerView.scrollToPosition(messageList.size - 1)
    }

    // 🔥 SAVE MESSAGE TO FIRESTORE
    private fun saveMessage(text: String, sender: String) {
        val userId = auth.currentUser?.uid ?: return

        val map = hashMapOf(
            "text" to text,
            "sender" to sender,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(userId)
            .collection("messages")
            .add(map)
    }

    // 🔥 LOAD OLD MESSAGES
    private fun loadMessages() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("messages")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { result ->
                messageList.clear()

                for (doc in result) {
                    val text = doc.getString("text") ?: ""
                    val sender = doc.getString("sender") ?: ""
                    val isUser = sender == "user"

                    messageList.add(ChatMessage(text, isUser))
                }

                adapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messageList.size - 1)
            }
    }

    // ✅ SPEAK
    private fun speakFallback(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    // 🔥 CHAT API
    private fun performChat(userInput: String) {
        lifecycleScope.launch {
            try {
                val messages = listOf(
                    Message(
                        role = "system",
                        content = "You are Albert Einstein. Speak simply and clearly."
                    ),
                    Message("user", userInput)
                )

                val request = ChatRequest(
                    model = "google/gemma-3-4b-it",
                    messages = messages
                )

                val token = "Bearer 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"

                val response = apiService.sendMessage(
                    token,
                    "https://lorexa.app",
                    "Lorexa",
                    request
                )

                val aiText = response.choices.firstOrNull()?.message?.content
                    ?: "No reply"

                addMessage(aiText, false)
                saveMessage(aiText, "ai") // 🔥 SAVE AI MESSAGE

                speakFallback(aiText)

            } catch (e: Exception) {
                addMessage("Error: ${e.message}", false)
                speakFallback("Something went wrong")
            }
        }
    }

    // 🎤 VOICE INPUT
    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = result?.get(0) ?: return

            addMessage(spokenText, true)
            saveMessage(spokenText, "user") // 🔥 SAVE VOICE MESSAGE
            performChat(spokenText)
        }
    }
}