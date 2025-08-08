package com.example.listedenalapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.listedenalapp.databinding.FragmentWelcomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)?.visibility = View.GONE

        binding.buttonRegister.setOnClickListener {
            // Kayıt sayfasına git
            (activity as? MainActivity)?.loadFragment(RegisterFragment())
        }

        binding.buttonLogin.setOnClickListener {
            // Giriş sayfasına git
            (activity as? MainActivity)?.loadFragment(LoginFragment())
        }

    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)?.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}