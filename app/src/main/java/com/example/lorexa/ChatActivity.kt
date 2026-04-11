package com.example.lorexa

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
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
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.WindowInsetsCompat.Type.ime



class ChatActivity : AppCompatActivity() {

    private var tts: TextToSpeech? = null
    private lateinit var apiService: ApiService
    private lateinit var adapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()
    private lateinit var recyclerView: RecyclerView
//    private var character: String = "Albert Einstein"
    // 🔥 FIREBASE
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var character: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chat)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
//
//        val root = findViewById<View>(R.id.rootLayout)
//
//        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
//
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//
//            view.setPadding(
//                systemBars.left,
//                systemBars.top,
//                systemBars.right,
//                0
//            )
//        insets
//    }

//        editText.setOnFocusChangeListener { _, hasFocus ->
//            if (hasFocus) {
//                recyclerView.postDelayed({
//                    recyclerView.scrollToPosition(messageList.size - 1)
//                }, 300)
//            }
//        }
        character = intent.getStringExtra("character") ?: "default"


        val backBtn = findViewById<ImageView>(R.id.backBtn)

        backBtn.setOnClickListener {
            finish()
        }
//        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setupCharacterUI(character)

//        val character = intent.getStringExtra("character") ?: "Character"

        val headerName = findViewById<TextView>(R.id.headerName)
        headerName.text = character

        // ✅ TTS INIT
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.ENGLISH
                tts?.setPitch(0.7f)
                tts?.setSpeechRate(0.85f)
            }
        }

        val micButton = findViewById<ImageView>(R.id.micButton)

        if (micButton == null) {
            Log.e("CRASH_DEBUG", "micButton NOT FOUND ❌")
        } else {
            micButton.setOnClickListener {
                Log.d("CLICK", "Mic clicked")
            }
        }
        val editText = findViewById<EditText>(R.id.editText)
        val sendButton = findViewById<ImageView>(R.id.sendButton)
        if (sendButton == null) {
            Log.e("DEBUG", "sendButton is NULL 💀")
        }
        recyclerView = findViewById(R.id.chatRecyclerView)
        adapter = ChatAdapter(messageList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                recyclerView.post {
                    recyclerView.scrollToPosition(messageList.size - 1)
                }
            }
        }
        loadMessages()
        sendButton.setOnClickListener {
            val text = editText.text.toString()
            if (text.isNotBlank()) {

                addMessage(text, true)

                saveMessage(text, "user", character)   // 🔥 ADD THIS LINE

                editText.text.clear()

                performChat(text)
            }
        }

        checkAchievement()

        Log.d("DEBUG", "SendButton: $sendButton")
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

        micButton.setOnClickListener {
            startVoiceInput()
        }
    }
    private fun setupCharacterUI(character: String) {

        val header = findViewById<TextView>(R.id.headerName)
        val chatBg = findViewById<View>(R.id.rootLayout)

        header.text = character

        when (character) {

            "Albert Einstein" -> {
                chatBg.setBackgroundResource(R.drawable.bg_einstein)
            }

            "Cleopatra" -> {
                chatBg.setBackgroundResource(R.drawable.bg_cleopatra)
            }

            "Leonardo da Vinci" -> {
                chatBg.setBackgroundResource(R.drawable.bg_davinci)
            }

            "Marie Curie" -> {
                chatBg.setBackgroundResource(R.drawable.bg_curie)
            }

            "Jane Austen" -> {
                chatBg.setBackgroundResource(R.drawable.bg_austen)
            }

            "Martin Luther" -> {
                chatBg.setBackgroundResource(R.drawable.bg_luther)
            }
        }
    }
    // ✅ ADD MESSAGE TO UI
    private fun addMessage(text: String, isUser: Boolean) {
        messageList.add(ChatMessage(text, isUser))
        adapter.notifyItemInserted(messageList.size - 1)
        recyclerView.scrollToPosition(messageList.size - 1)
    }

    // 🔥 SAVE MESSAGE TO FIRESTORE
    private fun saveMessage(text: String, sender: String, character: String) {

        val userId = auth.currentUser?.uid ?: return

        val map = hashMapOf(
            "text" to text,
            "sender" to sender,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(userId)
            .collection("messages")
            .document(character)   // 🔥 CHARACTER LEVEL
            .collection("chat")
            .add(map)

        Log.d("FIREBASE_SAVE", "Saved to character: $character")
    }

    // 🔥 LOAD OLD MESSAGES
    private fun loadMessages() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("messages")
            .document(character) // ✅ IMPORTANT
            .collection("chat")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { result ->
                messageList.clear()

                for (doc in result) {
                    val text = doc.getString("text") ?: ""
                    val sender = doc.getString("sender") ?: "ai"

                    messageList.add(ChatMessage(text, sender == "user"))
                }

                adapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messageList.size - 1)
            }
    }

    private fun checkAchievement() {

        val userId = auth.currentUser?.uid ?: return

        if (messageList.size >= 5) {

            val data = hashMapOf(
                "title" to "Time Traveler"
            )

            db.collection("users")
                .document(userId)
                .collection("achievements")
                .document("time_traveler")
                .set(data)
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
        val systemPrompt = when(character) {

            "Leonardo da Vinci" ->
                "You are Leonardo da Vinci. Speak creatively, like an inventor, artist and genius thinker. Be curious and imaginative."

            "Cleopatra" ->
                "You are Cleopatra. Speak like a powerful, confident and elegant queen."

            "Albert Einstein" ->
                "You are Albert Einstein. Explain things simply, scientifically and thoughtfully."

            "Marie Curie" ->
                "You are Marie Curie. Speak intelligently about science, discovery and persistence."

            "Jane Austen" ->
                "You are Jane Austen. Speak in a polite, literary and classic English tone."

            "Martin Luther" ->
                "You are Martin Luther. Speak like a bold reformer with strong beliefs."

            else ->
                "You are a historical figure."
        }
        lifecycleScope.launch {
            try {
                val messages = listOf(
                    Message("system", systemPrompt),
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
                val aiText = response.choices.firstOrNull()?.message?.content ?: "No reply"

                addMessage(aiText, false)

                saveMessage(aiText, "ai", character)   // 🔥 ADD THIS LINE
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
            saveMessage(spokenText, "user",character) // 🔥 SAVE VOICE MESSAGE
            performChat(spokenText)
        }
    }
}