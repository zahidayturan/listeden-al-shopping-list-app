package com.example.listedenalapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.listedenalapp.databinding.FragmentProfileBinding // View Binding için

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Burada Profil sayfası içeriği ile ilgili kodlar olacak
        binding.profileTextView.text = "Profil Fragment"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}