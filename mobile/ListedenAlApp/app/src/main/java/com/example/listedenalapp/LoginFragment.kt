package com.example.listedenalapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.listedenalapp.data.api.RetrofitClient
import com.example.listedenalapp.data.model.UserLoginRequest
import com.example.listedenalapp.databinding.FragmentLoginBinding
import com.example.listedenalapp.utils.AuthTokenManager
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // AuthTokenManager örneğini oluşturun
    private lateinit var authTokenManager: AuthTokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // onCreate'de veya onViewCreated'da manager'ı başlatın
        authTokenManager = AuthTokenManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTextWatchers()
        updateButtonState()

        binding.buttonLogin.setOnClickListener {
            // Hata mesajlarını temizle
            binding.editTextEmailLogin.error = null

            val email = binding.editTextEmailLogin.text.toString().trim()
            val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$".toRegex()
            val password = binding.editTextPasswordLogin.text.toString().trim()

            if (email.isEmpty()) {
                binding.editTextEmailLogin.error = "E-posta adresi boş bırakılamaz"
                binding.editTextEmailLogin.requestFocus()
                return@setOnClickListener
            }

            if (!email.matches(emailRegex)) {
                binding.editTextEmailLogin.error = "Lütfen geçerli bir e-posta adresi girin."
                binding.editTextEmailLogin.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.editTextPasswordLogin.requestFocus()
                return@setOnClickListener
            }

            // Her şey tamamsa API'yi çağır
            apiLogin(email, password)
        }

        binding.textViewRegisterPrompt.setOnClickListener {
            // Kayıt sayfasına git
            (activity as? AuthActivity)?.loadFragment(RegisterFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.editTextEmailLogin.addTextChangedListener(textWatcher)
        binding.editTextPasswordLogin.addTextChangedListener(textWatcher)
    }

    private fun updateButtonState() {
        val username = binding.editTextEmailLogin.text.toString().trim()
        val password = binding.editTextPasswordLogin.text.toString().trim()

        val isFormValid = username.isNotEmpty() && password.isNotEmpty()
        binding.buttonLogin.alpha = if (isFormValid) 1.0f else 0.6f
    }

    private fun apiLogin(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val request = UserLoginRequest(email, password)
                // RetrofitClient.instance yerine RetrofitClient.getClient(requireContext()) kullan
                val response = RetrofitClient.getClient(requireContext()).loginUser(request)
//does the cookbook have pictures in it
                // the rcipe is on page 12 of the cookbook
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    authResponse?.let {
                        Toast.makeText(context, "${it.message} Hoş geldiniz ${it.username}!", Toast.LENGTH_LONG).show()
                        authTokenManager.saveAuthToken(it.token)
                        Log.d("LoginFragment", "Token kaydedildi: ${it.token}")
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