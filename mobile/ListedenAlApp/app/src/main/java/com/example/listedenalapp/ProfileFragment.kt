package com.example.listedenalapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.listedenalapp.databinding.FragmentProfileBinding
import com.example.listedenalapp.utils.AuthTokenManager
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var authTokenManager: AuthTokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View { // Nullable View? yerine View kullanıldı
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // AuthTokenManager'ı başlatın
        authTokenManager = AuthTokenManager(requireContext())

        // Örnek metin güncellemeleri
        binding.profileTextView.text = "Merhaba! Burası Profiliniz."

        // Çıkış yap butonuna tıklama dinleyicisi ekle
        binding.buttonLogout.setOnClickListener {
            logout() // Artık bu fonksiyonu çağırabiliriz
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Bu fonksiyonu doğrudan bir UI elemanının click listener'ında çağıracağımız için
    // accessibility açısından private kalması uygun.
    private fun logout() {
        lifecycleScope.launch {
            authTokenManager.clearAuthToken()
            Toast.makeText(context, "Çıkış yapıldı.", Toast.LENGTH_SHORT).show()

            (activity as? MainActivity)?.loadFragment(LoginFragment())
            (activity as? MainActivity)?.setBottomNavigationSelectedItem(R.id.navigation_home)
        }
    }
}