package com.example.okimmo.ui

import com.example.okimmo.model.LoginResponse
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.okimmo.auth.TokenManager
import com.example.okimmo.api.ApiClient
import com.example.okimmo.api.AuthApi
import com.example.okimmo.model.LoginRequest
import kotlinx.coroutines.launch
import com.example.okimmo.MainActivity
import com.example.okimmo.databinding.ActivityLoginBinding
import androidx.core.view.isVisible
import com.example.okimmo.utils.ToastHelper
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var authApi: AuthApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeComponents()
        checkExistingToken()
        setupClickListeners()
    }

    private fun initializeComponents() {
        tokenManager = TokenManager(applicationContext)
        val retrofit = ApiClient.create(tokenManager)
        authApi = retrofit.create(AuthApi::class.java)
    }

    private fun checkExistingToken() {
        if (!tokenManager.getAccessToken().isNullOrEmpty()) {
            navigateToMain()
            return
        }
    }

    private fun setupClickListeners() {
        binding.loginBtn.setOnClickListener {
            performLogin()
        }

        binding.signupLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        try {
            binding.forgotPassword.setOnClickListener {
                ToastHelper.showSnackbar(
                    binding.root,
                    "Fonctionnalité à implémenter",
                    ToastHelper.ToastType.INFO
                )
            }
        } catch (e: Exception) {
            Log.e("LoginActivity", "Erreur forgotPassword: ${e.message}")
        }
    }

    private fun performLogin() {
        val email = binding.emailField.text.toString().trim()
        val password = binding.passwordField.text.toString().trim()

        if (!validateInputs(email, password)) {
            return
        }
        setLoadingState(true)
        lifecycleScope.launch {
            try {
                val response = authApi.login(LoginRequest(email, password))

                if (response.isSuccessful && response.body() != null) {
                    handleLoginSuccess(response.body()!!)
                } else {
                    handleLoginError(response.code())
                }
            } catch (e: Exception) {
                handleNetworkError(e)
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        when {
            email.isEmpty() || password.isEmpty() -> {
                ToastHelper.showSnackbar(
                    binding.root,
                    "Veuillez remplir tous les champs",
                    ToastHelper.ToastType.ERROR
                )
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                ToastHelper.showSnackbar(
                    binding.root,
                    "Email invalide",
                    ToastHelper.ToastType.ERROR
                )
                return false
            }
            password.length < 6 -> {
                ToastHelper.showSnackbar(
                    binding.root,
                    "Le mot de passe doit contenir au moins 6 caractères",
                    ToastHelper.ToastType.WARNING
                )
                return false
            }
        }
        return true
    }

    private fun handleLoginSuccess(response: LoginResponse) {
        tokenManager.saveTokens(response.token, response.refreshToken)
        tokenManager.saveUserInfos(response.user)

        ToastHelper.showSnackbar(
            binding.root,
            "Connexion réussie ! Bienvenue ${response.user.name}",
            ToastHelper.ToastType.SUCCESS,
            duration = Snackbar.LENGTH_SHORT
        )

        navigateToMain()
    }

    private fun handleLoginError(errorCode: Int) {
        val errorMessage = when (errorCode) {
            400 -> "Données invalides"
            401 -> "Email ou mot de passe incorrect"
            404 -> "Service non disponible"
            429 -> "Trop de tentatives. Réessayez plus tard"
            500 -> "Erreur serveur"
            else -> "Connexion échouée (Code: $errorCode)"
        }

        ToastHelper.showSnackbar(
            binding.root,
            errorMessage,
            ToastHelper.ToastType.ERROR,
            duration = Snackbar.LENGTH_LONG,
            actionText = "RÉESSAYER"
        ) {
            performLogin()
        }
    }

    private fun handleNetworkError(exception: Exception) {
        val errorMessage = when {
            exception.message?.contains("timeout", ignoreCase = true) == true ->
                "Timeout - Vérifiez votre connexion"
            exception.message?.contains("network", ignoreCase = true) == true ->
                "Erreur réseau - Vérifiez votre connexion"
            else -> "Erreur: ${exception.message}"
        }

        ToastHelper.showSnackbar(
            binding.root,
            errorMessage,
            ToastHelper.ToastType.ERROR,
            duration = Snackbar.LENGTH_LONG,
            actionText = "RÉESSAYER"
        ) {
            performLogin()
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.loginBtn.isEnabled = !isLoading
        binding.loginBtn.text = if (isLoading) {
            "Connexion..."
        } else {
            "SE CONNECTER"
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}