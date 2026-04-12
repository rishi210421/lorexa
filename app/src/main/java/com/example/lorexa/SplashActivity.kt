package com.example.lorexa


import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val videoView = findViewById<VideoView>(R.id.videoView)

        val videoUri = Uri.parse("android.resource://$packageName/${R.raw.intro}")
        videoView.setVideoURI(videoUri)

        videoView.setOnPreparedListener { mp ->

            val videoWidth = mp.videoWidth
            val videoHeight = mp.videoHeight

            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels

            val scaleX = screenWidth.toFloat() / videoWidth
            val scaleY = screenHeight.toFloat() / videoHeight

            val scale = maxOf(scaleX, scaleY)

            val params = videoView.layoutParams
            params.width = (videoWidth * scale).toInt()
            params.height = (videoHeight * scale).toInt()
            videoView.layoutParams = params

            videoView.start()
        }

        videoView.setOnPreparedListener { mp ->

            val videoWidth = mp.videoWidth
            val videoHeight = mp.videoHeight

            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels

            val scale = maxOf(
                screenWidth.toFloat() / videoWidth,
                screenHeight.toFloat() / videoHeight
            )

            val params = videoView.layoutParams
            params.width = (videoWidth * scale).toInt()
            params.height = (videoHeight * scale).toInt()
            videoView.layoutParams = params

            videoView.x = (screenWidth - params.width) / 2f
            videoView.y = (screenHeight - params.height) / 2f

            videoView.start()
        }

        videoView.setOnCompletionListener {

            // check user login
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser

            if (user != null) {
                // user already logged in
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                // new user → go login
                startActivity(Intent(this, LoginActivity::class.java))
            }

            finish()
        }

        videoView.start()
    }
}