package com.example.listedenalapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.listedenalapp.databinding.FragmentLoginBinding

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
            val username = binding.editTextUsernameLogin.text.toString()
            val password = binding.editTextPasswordLogin.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Kullanıcı adı ve şifre boş bırakılamaz.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: API üzerinden giriş isteği gönderme fonksiyonunu burada çağırın
            // Örneğin: apiLogin(username, password)
            Toast.makeText(context, "Giriş denemesi yapılıyor...", Toast.LENGTH_SHORT).show()
            // Geçici olarak başarılı giriş mesajı gösterelim
            // Eğer giriş başarılı olursa HomeFragment'a yönlendir
            (activity as? MainActivity)?.loadFragment(HomeFragment())
            Toast.makeText(context, "Giriş başarılı!", Toast.LENGTH_SHORT).show()

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

    // TODO: API ile iletişim kuracak fonksiyonu buraya ekleyin
    private fun apiLogin(username: String, password: String) {
        // Bu fonksiyon API'ye giriş isteği gönderecek
        // Şimdilik boş bırakıldı
    }
}