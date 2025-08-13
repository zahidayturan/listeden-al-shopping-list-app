package com.example.listedenalapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope // Coroutine'ler için
import com.example.listedenalapp.data.api.RetrofitClient
import com.example.listedenalapp.data.model.UserRegisterRequest
import com.example.listedenalapp.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch // Coroutine başlatmak için

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonRegister.setOnClickListener {
            val username = binding.editTextUsernameRegister.text.toString().trim()
            val email = binding.editTextEmailRegister.text.toString().trim()
            val password = binding.editTextPasswordRegister.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Tüm alanları doldurmanız gerekmektedir.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            (activity as? MainActivity)?.loadHomeFragment()
            // apiRegister(username, email, password)
        }

        binding.textViewLoginPrompt.setOnClickListener {
            // Giriş sayfasına git
            (activity as? AuthActivity)?.loadFragment(LoginFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun apiRegister(username: String, email: String, password: String) {
        lifecycleScope.launch {
            try {
                val request = UserRegisterRequest(username, email, password, "firstName", "lastName")
                val response = RetrofitClient.getClient(requireContext()).registerUser(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    authResponse?.let {
                        Toast.makeText(context, "${it.message} Lütfen giriş yapın.", Toast.LENGTH_LONG).show()
                        (activity as? MainActivity)?.loadFragment(LoginFragment())
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(context, "Kayıt başarısız: ${errorBody ?: "Bilinmeyen hata"}", Toast.LENGTH_LONG).show()
                    Log.e("RegisterFragment", "Kayıt hatası: ${response.code()} - ${errorBody}")
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Bağlantı hatası: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("RegisterFragment", "API çağrısı sırasında hata oluştu: ${e.message}", e)
            }
        }
    }
}