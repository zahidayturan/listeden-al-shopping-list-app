package com.example.listedenalapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton

import androidx.fragment.app.Fragment
import com.example.listedenalapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val notificationsButton: ImageButton = view.findViewById(R.id.openNotificationsButton)

        notificationsButton.setOnClickListener {
            println("Bildirimlere tıklandı!")
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}