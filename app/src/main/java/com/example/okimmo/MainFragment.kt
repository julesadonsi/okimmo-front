package com.example.okimmo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.okimmo.auth.TokenManager
import com.example.okimmo.ui.LoginActivity
import com.google.android.material.navigation.NavigationView

class MainFragment : Fragment() {

    private lateinit var tokenManager: TokenManager
    private lateinit var drawerLayout: DrawerLayout

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        tokenManager = TokenManager(requireContext())

        // Initialiser le DrawerLayout
        drawerLayout = view.findViewById(R.id.drawerLayout)
        val hamburgerBtn: ImageView = view.findViewById(R.id.hamburgerBtn)
        val navigationView: NavigationView = view.findViewById(R.id.navigationView)

        // Contenu principal
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

        // Ouvrir le drawer au clic sur le hamburger
        hamburgerBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Configurer le header du NavigationView
        val headerView = navigationView.getHeaderView(0)
        val userNameText: TextView = headerView.findViewById(R.id.userNameText)
        val userEmailText: TextView = headerView.findViewById(R.id.userEmailText)
        val userTokenText: TextView = headerView.findViewById(R.id.userTokenText)


        // Remplacer par les vraies données utilisateur
        val userInfos = tokenManager.getUserInfos()
        if (userInfos != null) {
            userNameText.text = userInfos.name
            userEmailText.text = userInfos.email
        } else {
            userNameText.text = "Utilisateur"
            userEmailText.text = "Non disponible"
        }

        userTokenText.text = if (token != null && token.length > 20) {
            "${token.take(15)}..."
        } else {
            token ?: "Non disponible"
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_settings -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_logout -> {
                    tokenManager.clear()
                    startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    requireActivity().finish()
                    true
                }
                else -> false
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Fermer le drawer si ouvert
        if (::drawerLayout.isInitialized && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }
}