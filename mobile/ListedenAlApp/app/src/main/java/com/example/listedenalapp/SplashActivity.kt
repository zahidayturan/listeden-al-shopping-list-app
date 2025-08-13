package com.example.listedenalapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.listedenalapp.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Bir gecikme eklemek için Handler kullanıyoruz.
        // Genellikle 2000-3000 ms (2-3 saniye) yeterlidir.
        Handler().postDelayed(object : Runnable {
            override fun run() {
                // Burada kontrol işlemlerini yapabiliriz.
                val isLoggedIn = checkIfUserIsLoggedIn()

                val intent: Intent?
                if (isLoggedIn) {
                    // Eğer kullanıcı giriş yapmışsa MainActivity'ye git.
                    intent = Intent(this@SplashActivity, MainActivity::class.java)
                } else {
                    // Kullanıcı giriş yapmamışsa AuthActivity'ye git.
                    intent = Intent(this@SplashActivity, AuthActivity::class.java)
                }

                startActivity(intent)

                // Splash Activity'yi kapatıyoruz ki geri tuşuna basınca tekrar gelmesin.
                finish()
            }
        }, 1000) // 2 saniye bekle
    }

    private fun checkIfUserIsLoggedIn(): Boolean {
        // Gerçek uygulamada burada SharedPreferences'dan bir token'ı kontrol edebilirsiniz.
        // Şimdilik varsayılan olarak 'false' dönüyoruz.
        return false
    }
}