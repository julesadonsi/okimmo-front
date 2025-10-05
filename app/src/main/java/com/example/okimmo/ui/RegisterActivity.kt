package com.example.okimmo.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.okimmo.MainActivity
import com.example.okimmo.R
import com.example.okimmo.api.ApiClient
import com.example.okimmo.api.AuthApi
import com.example.okimmo.auth.TokenManager
import com.example.okimmo.databinding.ActivityRegisterBinding
import com.example.okimmo.model.LoginResponse
import com.example.okimmo.model.RegisterRequest
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var authApi: AuthApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeComponents()
        checkExistingToken()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.registerBtn.setOnClickListener {
            performRegister()
        }
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

    private fun performRegister() {
        val name = binding.fullnameField.text.toString().trim()
        val email = binding.emailField.text.toString().trim()
        val password = binding.passwordField.text.toString().trim()
        val passwordConfirmation = binding.confirmPasswordField.text.toString().trim()

        if (!validateInputs(name, email, password, passwordConfirmation)) {
            return
        }
        setLoadingState(true)
        lifecycleScope.launch {
            try {
                val response = authApi.register(RegisterRequest(name, email, password))

                if (response.isSuccessful && response.body() != null) {
                    handleRegisterSuccess(response.body()!!)
                } else {
                    handleRegisterError(response.code())
                }
            } catch (e: Exception) {
                handleNetworkError(e)
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun validateInputs(name: String, email: String, password: String, passwordConfirm: String): Boolean {
        when {
            name.isEmpty() || email.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty() -> {
                showToast("Veuillez remplir tous les champs")
                return false
            }
            name.length < 2 -> {
                showToast("Le nom doit contenir au moins 2 caractères")
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showToast("Email invalide")
                return false
            }
            password.length < 6 -> {
                showToast("Le mot de passe doit contenir au moins 6 caractères")
                return false
            }
            password != passwordConfirm -> {
                showToast("Les deux mots de passe ne correspondent pas")
                return false
            }
        }
        return true
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.registerBtn.isEnabled = !isLoading
        binding.registerBtn.text = if (isLoading) {
            "Inscription..."
        } else {
            getString(R.string.register)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun handleRegisterSuccess(response: LoginResponse) {
        tokenManager.saveTokens(response.token, response.refreshToken)
        showToast("Inscription réussie")
        navigateToMain()
    }

    private fun handleRegisterError(errorCode: Int) {
        val errorMessage = when (errorCode) {
            400 -> "Données invalides"
            409 -> "Un compte existe déjà avec cet email"
            422 -> "Données de validation échouées"
            429 -> "Trop de tentatives. Réessayez plus tard"
            500 -> "Erreur serveur"
            else -> "Inscription échouée (Code: $errorCode)"
        }
        showToast(errorMessage)
    }

    private fun handleNetworkError(exception: Exception) {
        val errorMessage = when {
            exception.message?.contains("timeout", ignoreCase = true) == true ->
                "Timeout - Vérifiez votre connexion"
            exception.message?.contains("network", ignoreCase = true) == true ->
                "Erreur réseau - Vérifiez votre connexion"
            else -> "Erreur: ${exception.message}"
        }
        showToast(errorMessage)
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}