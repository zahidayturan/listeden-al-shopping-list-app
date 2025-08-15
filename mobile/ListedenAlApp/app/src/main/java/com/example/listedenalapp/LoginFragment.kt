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
import com.google.gson.Gson
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

            /*if (!email.matches(emailRegex)) {
                binding.editTextEmailLogin.error = "Lütfen geçerli bir e-posta adresi girin."
                binding.editTextEmailLogin.requestFocus()
                return@setOnClickListener
            }*/

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
                val response = RetrofitClient.getClient(requireContext()).loginUser(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    authResponse?.let {
                        Toast.makeText(context, "Hoş geldiniz !", Toast.LENGTH_LONG).show()
                        authTokenManager.saveAuthToken(it.accessToken)
                        (activity as? AuthActivity)?.navigateToHome()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMap: Map<String, String>? = try {
                        Gson().fromJson(errorBody, Map::class.java) as Map<String, String>
                    } catch (e: Exception) {
                        null
                    }

                    val errorKey = errorMap?.get("error")
                    val errorMessage = when (errorKey) {
                        "invalid_credentials" -> getString(R.string.error_invalid_credentials)
                        "user_not_found" -> getString(R.string.error_user_not_found)
                        "authentication_failed" -> getString(R.string.error_authentication_failed)
                        "server_error" -> getString(R.string.error_server_error)
                        else -> getString(R.string.error_unknown)
                    }

                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    Log.e("LoginFragment", "Giriş hatası: ${response.code()} - Key: ${errorKey}")
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Bağlantı hatası: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("LoginFragment", "API çağrısı sırasında hata oluştu: ${e.message}", e)
            }
        }
    }
}