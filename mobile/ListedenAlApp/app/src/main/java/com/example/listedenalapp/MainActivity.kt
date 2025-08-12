package com.example.listedenalapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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
                loadFragment(WelcomeFragment())
            }
        }
    }

    fun loadFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // Fragment'ı değiştirme işlemi
        fragmentTransaction.replace(R.id.fragment_container, fragment)

        // Bu satır, geri tuşuna basıldığında bir önceki fragment'a dönmeyi sağlar.
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    fun loadHomeFragment() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, HomeFragment())
        fragmentTransaction.commit()
    }
    fun setBottomNavigationSelectedItem(itemId: Int) {
        binding.bottomNavigation.selectedItemId = itemId
    }
}