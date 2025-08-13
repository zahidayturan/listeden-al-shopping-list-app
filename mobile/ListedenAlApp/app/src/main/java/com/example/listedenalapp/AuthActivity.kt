package com.example.listedenalapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.listedenalapp.databinding.ActivityAuthBinding
import com.example.listedenalapp.utils.AuthTokenManager
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var authTokenManager: AuthTokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authTokenManager = AuthTokenManager(applicationContext)

        // Uygulama açıldığında ilk ve tek kontrolü burada yapıyoruz.
        // Bu Activity'nin ana amacı yönlendirme.
        checkAuthTokenAndNavigate()
    }

    private fun checkAuthTokenAndNavigate() {
        lifecycleScope.launch {
            val token = authTokenManager.getAuthToken()
            if (token != null) {
                val intent = Intent(this@AuthActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                loadFragment(WelcomeFragment())
            }
        }
    }

    fun loadFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.auth_fragment_container, fragment)
        fragmentTransaction.commit()
    }
}