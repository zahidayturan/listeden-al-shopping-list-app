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
import com.example.listedenalapp.data.model.UserLoginRequest
import com.example.listedenalapp.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch // Coroutine başlatmak için

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonLogin.setOnClickListener {
            val username = binding.editTextUsernameLogin.text.toString().trim()
            val password = binding.editTextPasswordLogin.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Kullanıcı adı ve şifre boş bırakılamaz.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            apiLogin(username, password)
        }

        binding.textViewRegisterPrompt.setOnClickListener {
            // Kayıt sayfasına git
            (activity as? MainActivity)?.loadFragment(RegisterFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun apiLogin(username: String, password: String) {
        // UI'yı bloklamamak için coroutine kullan
        lifecycleScope.launch {
            try {
                val request = UserLoginRequest(username, password)
                val response = RetrofitClient.instance.loginUser(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    authResponse?.let {
                        Toast.makeText(context, "${it.message} Hoş geldiniz ${it.username}!", Toast.LENGTH_LONG).show()
                        // TODO: Auth token'ı SharedPreferences veya DataStore'a kaydet
                        // navigate to HomeFragment
                        (activity as? MainActivity)?.loadFragment(HomeFragment())
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(context, "Giriş başarısız: ${errorBody ?: "Bilinmeyen hata"}", Toast.LENGTH_LONG).show()
                    Log.e("LoginFragment", "Giriş hatası: ${response.code()} - ${errorBody}")
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Bağlantı hatası: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("LoginFragment", "API çağrısı sırasında hata oluştu: ${e.message}", e)
            }
        }
    }
}