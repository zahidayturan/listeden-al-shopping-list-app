package com.example.listedenalapp

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
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

        (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.visibility = View.GONE

        setupPrivacyPolicyText()

        binding.buttonRegister.setOnClickListener {
            // Kayıt sayfasına git
            (activity as? MainActivity)?.loadFragment(RegisterFragment())
        }

        binding.buttonLogin.setOnClickListener {
            // Giriş sayfasına git
            (activity as? MainActivity)?.loadFragment(LoginFragment())
        }
    }

    private fun setupPrivacyPolicyText() {
        val fullText = getString(R.string.privacy_policy_text)
        val spannableString = SpannableString(fullText)
        val greenColor = ContextCompat.getColor(requireContext(), R.color.green_700)

        val cookiePolicyText = "Çerez Kullanımı"
        val termsOfServiceText = "Hizmet Şartları"
        val privacyPolicyText = "Gizlilik Politikası"

        setClickableSpan(spannableString, fullText, cookiePolicyText, greenColor)
        setClickableSpan(spannableString, fullText, termsOfServiceText, greenColor)
        setClickableSpan(spannableString, fullText, privacyPolicyText, greenColor)

        binding.privacyPolicyTextView.text = spannableString
        binding.privacyPolicyTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setClickableSpan(
        spannableString: SpannableString,
        fullText: String,
        targetText: String,
        color: Int
    ) {
        val start = fullText.indexOf(targetText)
        val end = start + targetText.length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(requireContext(), "$targetText tıklandı!", Toast.LENGTH_SHORT).show()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = color
            }
        }

        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    override fun onPause() {
        super.onPause()
        (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}