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
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authTokenManager = AuthTokenManager(requireContext())
        binding.profileTextView.text = "Merhaba! Burası Profiliniz."

        binding.buttonLogout.setOnClickListener {
            logout()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun logout() {
        lifecycleScope.launch {
            Toast.makeText(context, "Çıkış yapıldı.", Toast.LENGTH_SHORT).show()
            (activity as? MainActivity)?.logoutAndNavigateToAuth()
        }
    }
}