package com.example.listedenalapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            val isLoggedIn = checkIfUserIsLoggedIn()

            val intent: Intent?
            if (isLoggedIn) {
                intent = Intent(this@SplashActivity, MainActivity::class.java)
            } else {

                intent = Intent(this@SplashActivity, AuthActivity::class.java)
            }

            startActivity(intent)
            finish()
        }, 1000)
    }

    private fun checkIfUserIsLoggedIn(): Boolean {
        // Burada SharedPreferences'dan bir token'ı kontrol et.
        // Şimdilik varsayılan olarak 'false' dönüyoruz.
        return false
    }
}