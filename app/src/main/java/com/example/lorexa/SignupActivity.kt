package com.example.lorexa
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lorexa.ChatActivity
import com.example.lorexa.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        val name = findViewById<EditText>(R.id.name)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val btn = findViewById<Button>(R.id.signupBtn)

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        btn.setOnClickListener {

            val userName = name.text.toString()
            val userEmail = email.text.toString()
            val userPass = password.text.toString()

            if (userName.isEmpty() || userEmail.isEmpty() || userPass.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(userEmail, userPass)
                .addOnCompleteListener {

                    if (it.isSuccessful) {

                        val userId = auth.currentUser?.uid

                        val userMap = hashMapOf(
                            "name" to userName,
                            "email" to userEmail,
                            "createdAt" to System.currentTimeMillis()
                        )

                        if (userId != null) {
                            db.collection("users")
                                .document(userId)
                                .set(userMap)
                                .addOnSuccessListener {

                                    Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()

                                    startActivity(Intent(this, HomeActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show()
                                }
                        }

                    } else {
                        Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}