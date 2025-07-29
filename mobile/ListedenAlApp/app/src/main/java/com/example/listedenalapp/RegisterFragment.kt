package com.example.listedenalapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.listedenalapp.databinding.FragmentRegisterBinding

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
            val username = binding.editTextUsernameRegister.text.toString()
            val email = binding.editTextEmailRegister.text.toString()
            val password = binding.editTextPasswordRegister.text.toString()
            val firstName = binding.editTextFirstNameRegister.text.toString()
            val lastName = binding.editTextLastNameRegister.text.toString()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(context, "Tüm alanları doldurmanız gerekmektedir.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: API üzerinden kayıt isteği gönderme fonksiyonunu burada çağırın
            // Örneğin: apiRegister(username, email, password, firstName, lastName)
            Toast.makeText(context, "Kayıt denemesi yapılıyor...", Toast.LENGTH_SHORT).show()
            // Geçici olarak başarılı kayıt mesajı gösterelim
            // Eğer kayıt başarılı olursa LoginFragment'a yönlendir
            (activity as? MainActivity)?.loadFragment(LoginFragment())
            Toast.makeText(context, "Kayıt başarılı! Şimdi giriş yapabilirsiniz.", Toast.LENGTH_SHORT).show()
        }

        binding.textViewLoginPrompt.setOnClickListener {
            // Giriş sayfasına git
            (activity as? MainActivity)?.loadFragment(LoginFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // TODO: API ile iletişim kuracak fonksiyonu buraya ekleyin
    private fun apiRegister(username: String, email: String, password: String, firstName: String, lastName: String) {
        // Bu fonksiyon API'ye kayıt isteği gönderecek
        // Şimdilik boş bırakıldı
    }
}