package com.example.listedenalapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.listedenalapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Uygulama ilk açıldığında varsayılan olarak Giriş Sayfası'nı yükle
        // Eğer kullanıcı zaten giriş yapmışsa HomeFragment'a yönlendirebilirsiniz.
        if (savedInstanceState == null) {
            loadFragment(LoginFragment()) // Uygulama başlangıcında LoginFragment'ı yükle
        }

        // Bottom Navigation Bar'daki item tıklamalarını dinle
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.navigation_lists -> {
                    loadFragment(ListsFragment())
                    true
                }
                R.id.navigation_profile -> {
                    // Profil sekmesine tıklandığında Giriş sayfasına veya Profil sayfasına yönlendir
                    // Burada bir kontrol yaparak kullanıcının giriş yapıp yapmadığını kontrol edebilirsiniz.
                    // Şimdilik direkt LoginFragment'a yönlendiriyoruz.
                    loadFragment(LoginFragment())
                    true
                }
                else -> false
            }
        }
    }

    // Belirtilen Fragment'ı FrameLayout'a yükleyen yardımcı fonksiyon
    fun loadFragment(fragment: Fragment) { // Public olarak değiştirildi
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}