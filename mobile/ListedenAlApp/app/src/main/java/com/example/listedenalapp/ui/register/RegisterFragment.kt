package com.example.listedenalapp.ui.register

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.listedenalapp.AuthActivity
import com.example.listedenalapp.data.api.RetrofitClient
import com.example.listedenalapp.data.model.UserRegisterRequest
import com.example.listedenalapp.data.repository.AuthRepository
import com.example.listedenalapp.databinding.FragmentRegisterBinding
import com.example.listedenalapp.ui.login.LoginFragment
import com.example.listedenalapp.utils.AuthTokenManager

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var authTokenManager: AuthTokenManager
    private val registerViewModel: RegisterViewModel by activityViewModels {
        RegisterViewModelFactory(AuthRepository(RetrofitClient.getClient(requireContext())), authTokenManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authTokenManager = AuthTokenManager(requireContext())
    }

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

        registerViewModel.registerResult.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    setLoadingState(true)
                }
                is NetworkResult.Success -> {
                    setLoadingState(false)
                    Toast.makeText(context, "Kayıt başarılı! Hoş geldiniz!", Toast.LENGTH_LONG).show()
                    (activity as? AuthActivity)?.navigateToHome()
                }
                is NetworkResult.Error -> {
                    setLoadingState(false)
                    val errorMessage = result.message
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        })

        binding.buttonRegister.setOnClickListener {
            val username = binding.editTextUsernameRegister.text.toString().trim()
            val email = binding.editTextEmailRegister.text.toString().trim()
            val password = binding.editTextPasswordRegister.text.toString().trim()

            // Input validation
            if (username.isEmpty() || username.length < 3) {
                binding.editTextUsernameRegister.error = "Kullanıcı adı en az 3 karakter olmalı"
                binding.editTextUsernameRegister.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                binding.editTextEmailRegister.error = "E-posta adresi boş bırakılamaz"
                binding.editTextEmailRegister.requestFocus()
                return@setOnClickListener
            }

            val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$".toRegex()
            if (!email.matches(emailRegex)) {
                binding.editTextEmailRegister.error = "Lütfen geçerli bir e-posta adresi girin."
                binding.editTextEmailRegister.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty() || password.length < 8) {
                binding.editTextPasswordRegister.error = "Şifre en az 8 karakter olmalı"
                binding.editTextPasswordRegister.requestFocus()
                return@setOnClickListener
            }

            // Submit form for registration
            val userRegisterRequest = UserRegisterRequest(username, email, password)
            registerViewModel.registerUser(userRegisterRequest)
        }

        binding.textViewLoginPrompt.setOnClickListener {
            (activity as? AuthActivity)?.loadFragment(LoginFragment())
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.buttonRegister.isEnabled = !isLoading
        if (isLoading) {
            binding.buttonRegister.alpha = 0.6f
            binding.registerButtonProgress.visibility = View.VISIBLE
        } else {
            updateButtonState()
            binding.registerButtonProgress.visibility = View.GONE
        }
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
        val username = binding.editTextUsernameRegister.text.toString().trim()
        val email = binding.editTextEmailRegister.text.toString().trim()
        val password = binding.editTextPasswordRegister.text.toString().trim()

        val isFormValid = username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()
        binding.buttonRegister.alpha = if (isFormValid) 1.0f else 0.6f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
