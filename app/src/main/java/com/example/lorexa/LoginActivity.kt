package com.example.lorexa
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        setContentView(R.layout.activity_login)

        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val loginBtn = findViewById<Button>(R.id.loginBtn)

        loginBtn.setOnClickListener {
            auth.signInWithEmailAndPassword(
                email.text.toString(),
                password.text.toString()
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    startActivity(Intent(this, ChatActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun goToSignup(view: View) {
        startActivity(Intent(this, SignupActivity::class.java))
    }

    fun goToReset(view: View) {
        startActivity(Intent(this, ResetPasswordActivity::class.java))
    }
}