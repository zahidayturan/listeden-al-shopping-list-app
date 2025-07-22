package com.example.listedenalapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.listedenalapp.databinding.FragmentListsBinding // View Binding için

class ListsFragment : Fragment() {

    private var _binding: FragmentListsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Burada Listelerim sayfası içeriği ile ilgili kodlar olacak
        binding.listsTextView.text = "Listelerim Fragment"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}