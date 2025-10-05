package com.example.okimmo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.okimmo.auth.TokenManager
import com.example.okimmo.ui.LoginActivity

class MainFragment : Fragment() {

    private lateinit var tokenManager: TokenManager

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        tokenManager = TokenManager(requireContext())

        val welcomeText: TextView = view.findViewById(R.id.welcomeText)
        val tokenText: TextView = view.findViewById(R.id.tokenText)
        val logoutBtn: Button = view.findViewById(R.id.logoutBtn)

        welcomeText.text = "Bienvenue dans l'application !"

        val token = tokenManager.getAccessToken()
        tokenText.text = if (token != null && token.length > 20) {
            "Token: ${token.take(10)}...${token.takeLast(10)}"
        } else {
            "Token: ${token ?: "Non disponible"}"
        }

        logoutBtn.setOnClickListener {
            tokenManager.clear()
            startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            requireActivity().finish()
        }

        return view
    }
}