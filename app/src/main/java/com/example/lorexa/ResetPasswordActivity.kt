package com.example.lorexa

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        auth = FirebaseAuth.getInstance()

        val email = findViewById<EditText>(R.id.email)
        val btn = findViewById<Button>(R.id.resetBtn)

        btn.setOnClickListener {
            auth.sendPasswordResetEmail(email.text.toString())
                .addOnCompleteListener {
                    Toast.makeText(this, "Reset link sent", Toast.LENGTH_SHORT).show()
                }
        }
    }
}