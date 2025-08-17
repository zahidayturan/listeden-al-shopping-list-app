package com.example.listedenalapp.ui.login

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
import com.example.listedenalapp.RegisterFragment
import com.example.listedenalapp.data.api.RetrofitClient
import com.example.listedenalapp.data.repository.AuthRepository
import com.example.listedenalapp.databinding.FragmentLoginBinding
import com.example.listedenalapp.utils.AuthTokenManager

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var authTokenManager: AuthTokenManager
    private val loginViewModel: LoginViewModel by activityViewModels {
        LoginViewModelFactory(AuthRepository(RetrofitClient.getClient(requireContext())), authTokenManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        loginViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.buttonLogin.isEnabled = !isLoading
            binding.textViewRegisterPrompt.isClickable = !isLoading
            if (isLoading) {
                binding.buttonLogin.alpha = 0.6f
                binding.loginButtonProgress.visibility = View.VISIBLE
            } else {
                updateButtonState()
                binding.loginButtonProgress.visibility = View.GONE
            }
        })

        binding.buttonLogin.setOnClickListener {
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

            loginViewModel.loginUser(email, password, onSuccess = {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                (activity as? AuthActivity)?.navigateToHome()
            }, onError = {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            })
        }

        binding.textViewRegisterPrompt.setOnClickListener {
            (activity as? AuthActivity)?.loadFragment(RegisterFragment())
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

        binding.editTextEmailLogin.addTextChangedListener(textWatcher)
        binding.editTextPasswordLogin.addTextChangedListener(textWatcher)
    }

    private fun updateButtonState() {
        val username = binding.editTextEmailLogin.text.toString().trim()
        val password = binding.editTextPasswordLogin.text.toString().trim()

        val isFormValid = username.isNotEmpty() && password.isNotEmpty()
        binding.buttonLogin.alpha = if (isFormValid) 1.0f else 0.6f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}