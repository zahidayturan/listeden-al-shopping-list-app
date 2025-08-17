package com.example.listedenalapp.ui.register

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
import com.example.listedenalapp.AuthActivity
import com.example.listedenalapp.data.api.RetrofitClient
import com.example.listedenalapp.data.model.UserRegisterRequest
import com.example.listedenalapp.databinding.FragmentRegisterBinding
import com.example.listedenalapp.ui.login.LoginFragment
import kotlinx.coroutines.launch

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

        setupTextWatchers()
        updateButtonState()

        binding.buttonRegister.setOnClickListener {
            val username = binding.editTextUsernameRegister.text.toString().trim()
            val email = binding.editTextEmailRegister.text.toString().trim()
            val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$".toRegex()
            val password = binding.editTextPasswordRegister.text.toString().trim()
            val errorMessage = when {
                password.isEmpty() -> "Şifre alanı boş bırakılamaz."
                password.length < 8 -> "Şifre en az 8 karakter olmalıdır."
                !password.any { it.isDigit() } -> "Şifre en az bir rakam içermelidir."
                else -> null
            }

            if (email.isEmpty()) {
                binding.editTextEmailRegister.error = "E-posta adresi boş bırakılamaz"
                binding.editTextEmailRegister.requestFocus()
                return@setOnClickListener
            }

            if (!email.matches(emailRegex)) {
                binding.editTextEmailRegister.error = "Lütfen geçerli bir e-posta adresi girin."
                binding.editTextEmailRegister.requestFocus()
                return@setOnClickListener
            }

            if (username.isEmpty() || username.length < 3) {
                binding.editTextUsernameRegister.error = "Kullanıcı adı en az 3 karakter olmalı"
                binding.editTextUsernameRegister.requestFocus()
                return@setOnClickListener
            }

            if (errorMessage != null) {
                binding.textViewPasswordError.text = errorMessage
                binding.textViewPasswordError.visibility = View.VISIBLE
                binding.editTextPasswordRegister.requestFocus()
                return@setOnClickListener
            } else {
                binding.textViewPasswordError.visibility = View.GONE
            }

            apiRegister(username, email, password)
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

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.editTextEmailRegister.addTextChangedListener(textWatcher)
        binding.editTextUsernameRegister.addTextChangedListener(textWatcher)
        binding.editTextPasswordRegister.addTextChangedListener(textWatcher)
    }

    private fun updateButtonState() {
        val email = binding.editTextEmailRegister.text.toString().trim()
        val username = binding.editTextUsernameRegister.text.toString().trim()
        val password = binding.editTextPasswordRegister.text.toString().trim()

        val isFormValid = username.isNotEmpty() && password.isNotEmpty() && email.isNotEmpty()
        binding.buttonRegister.alpha = if (isFormValid) 1.0f else 0.6f
    }

    private fun apiRegister(username: String, email: String, password: String) {
        (activity as? AuthActivity)?.navigateToHome()

        /*lifecycleScope.launch {
            try {
                val request =
                    UserRegisterRequest(username, email, password)
                val response = RetrofitClient.getClient(requireContext()).registerUser(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    authResponse?.let {
                        Toast.makeText(context, "Lütfen giriş yapın.", Toast.LENGTH_LONG).show()
                        (activity as? AuthActivity)?.loadFragment(LoginFragment())
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(context, "Kayıt başarısız: ${errorBody ?: "Bilinmeyen hata"}", Toast.LENGTH_LONG).show()
                    Log.e("RegisterFragment", "Kayıt hatası: ${response.code()} - ${errorBody}")
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Bağlantı hatası", Toast.LENGTH_LONG).show()
                Log.e("RegisterFragment", "API çağrısı sırasında hata oluştu: ${e.message}", e)
            }
        }*/
    }
}