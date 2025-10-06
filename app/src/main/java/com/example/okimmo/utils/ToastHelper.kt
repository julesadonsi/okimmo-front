package com.example.okimmo.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.okimmo.R
import com.google.android.material.snackbar.Snackbar
import android.view.View

object ToastHelper {

    enum class ToastType {
        SUCCESS, ERROR, INFO, WARNING
    }

    /**
     * Affiche un Toast personnalisé avec couleur et icône
     */
    fun show(
        context: Context,
        message: String,
        type: ToastType = ToastType.INFO,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        val toast = Toast.makeText(context, message, duration)

        // Personnaliser l'apparence selon le type
        toast.view?.apply {
            val backgroundColor = when (type) {
                ToastType.SUCCESS -> ContextCompat.getColor(context, R.color.success_green)
                ToastType.ERROR -> ContextCompat.getColor(context, R.color.error_red)
                ToastType.WARNING -> ContextCompat.getColor(context, R.color.warning_orange)
                ToastType.INFO -> ContextCompat.getColor(context, R.color.info_blue)
            }
            setBackgroundColor(backgroundColor)

            // Personnaliser le texte
            findViewById<TextView>(android.R.id.message)?.apply {
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                textSize = 16f
                setPadding(24, 16, 24, 16)
            }
        }

        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 100)
        toast.show()
    }

    /**
     * Affiche un Snackbar (recommandé par Material Design)
     */
    fun showSnackbar(
        view: View,
        message: String,
        type: ToastType = ToastType.INFO,
        duration: Int = Snackbar.LENGTH_SHORT,
        actionText: String? = null,
        actionCallback: (() -> Unit)? = null
    ) {
        val snackbar = Snackbar.make(view, message, duration)

        // Couleur de fond selon le type
        val backgroundColor = when (type) {
            ToastType.SUCCESS -> ContextCompat.getColor(view.context, R.color.success_green)
            ToastType.ERROR -> ContextCompat.getColor(view.context, R.color.error_red)
            ToastType.WARNING -> ContextCompat.getColor(view.context, R.color.warning_orange)
            ToastType.INFO -> ContextCompat.getColor(view.context, R.color.info_blue)
        }

        snackbar.view.setBackgroundColor(backgroundColor)
        snackbar.setTextColor(ContextCompat.getColor(view.context, android.R.color.white))

        // Ajouter une action si nécessaire
        if (actionText != null && actionCallback != null) {
            snackbar.setAction(actionText) { actionCallback() }
            snackbar.setActionTextColor(ContextCompat.getColor(view.context, android.R.color.white))
        }

        snackbar.show()
    }

    /**
     * Méthodes raccourcies pour chaque type
     */
    fun success(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        show(context, "✓ $message", ToastType.SUCCESS, duration)
    }

    fun error(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        show(context, "✗ $message", ToastType.ERROR, duration)
    }

    fun warning(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        show(context, "⚠ $message", ToastType.WARNING, duration)
    }

    fun info(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        show(context, "ℹ $message", ToastType.INFO, duration)
    }

    /**
     * Snackbar raccourcies
     */
    fun successSnackbar(view: View, message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        showSnackbar(view, "✓ $message", ToastType.SUCCESS, duration)
    }

    fun errorSnackbar(view: View, message: String, duration: Int = Snackbar.LENGTH_LONG) {
        showSnackbar(view, "✗ $message", ToastType.ERROR, duration)
    }
}