package com.example.okimmo.ui

import android.content.Intent
import android.os.Bundle
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
import com.example.okimmo.utils.ToastHelper
import com.google.android.material.snackbar.Snackbar
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
                ToastHelper.showSnackbar(
                    binding.root,
                    "Veuillez remplir tous les champs",
                    ToastHelper.ToastType.ERROR
                )
                return false
            }
            name.length < 2 -> {
                ToastHelper.showSnackbar(
                    binding.root,
                    "Le nom doit contenir au moins 2 caractères",
                    ToastHelper.ToastType.WARNING
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
            password != passwordConfirm -> {
                ToastHelper.showSnackbar(
                    binding.root,
                    "Les mots de passe ne correspondent pas",
                    ToastHelper.ToastType.ERROR,
                    duration = Snackbar.LENGTH_LONG
                )
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

    private fun handleRegisterSuccess(response: LoginResponse) {
        tokenManager.saveTokens(response.token, response.refreshToken)
        tokenManager.saveUserInfos(response.user)

        ToastHelper.showSnackbar(
            binding.root,
            "Inscription réussie ! Bienvenue ${response.user.name} 🎉",
            ToastHelper.ToastType.SUCCESS,
            duration = Snackbar.LENGTH_LONG
        )

        // Attendre un peu avant de naviguer pour que l'utilisateur voie le message
        binding.root.postDelayed({
            navigateToMain()
        }, 1500)
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

        ToastHelper.showSnackbar(
            binding.root,
            errorMessage,
            ToastHelper.ToastType.ERROR,
            duration = Snackbar.LENGTH_LONG,
            actionText = if (errorCode == 409) "SE CONNECTER" else "RÉESSAYER"
        ) {
            if (errorCode == 409) {
                // Si l'email existe déjà, rediriger vers le login
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                // Sinon réessayer l'inscription
                performRegister()
            }
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
            performRegister()
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}