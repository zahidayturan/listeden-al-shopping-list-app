package com.example.listedenalapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.listedenalapp.databinding.ActivityMainBinding
import com.example.listedenalapp.utils.AuthTokenManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var authTokenManager: AuthTokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authTokenManager = AuthTokenManager(applicationContext)

        if (savedInstanceState == null) {
            checkAuthTokenAndNavigate()
        }

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
                    checkAuthTokenAndNavigate()
                    true
                }
                else -> false
            }
        }
    }

    private fun checkAuthTokenAndNavigate() {
        lifecycleScope.launch {
            val token = authTokenManager.getAuthToken()
            if (token != null) {
                loadFragment(HomeFragment())
            } else {
                loadFragment(LoginFragment())
            }
        }
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun setBottomNavigationSelectedItem(itemId: Int) {
        binding.bottomNavigation.selectedItemId = itemId
    }
}