package com.cloudstorage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var apiClient: ApiClient
    
    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var progressBar: View
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        apiClient = ApiClient(this)
        
        initViews()
        setupClickListeners()
    }
    
    private fun initViews() {
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        progressBar = findViewById(R.id.progressBar)
    }
    
    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            
            if (validateInput(username, email, password)) {
                performRegister(username, email, password)
            }
        }
        
        findViewById<View>(R.id.tvLogin).setOnClickListener {
            finish()
        }
    }
    
    private fun validateInput(username: String, email: String, password: String): Boolean {
        when {
            username.isEmpty() -> {
                etUsername.error = getString(R.string.username_required)
                return false
            }
            email.isEmpty() -> {
                etEmail.error = getString(R.string.email_required)
                return false
            }
            password.isEmpty() -> {
                etPassword.error = getString(R.string.password_required)
                return false
            }
            !isValidEmail(email) -> {
                etEmail.error = "Please enter a valid email address"
                return false
            }
        }
        return true
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun performRegister(username: String, email: String, password: String) {
        btnRegister.isEnabled = false
        progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    apiClient.register(username, email, password)
                }
                
                if (result.isSuccess) {
                    Toast.makeText(this@RegisterActivity, 
                        result.getOrNull() ?: "Registration successful! Please login.",
                        Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    showError(result.exceptionOrNull()?.message ?: "Registration failed")
                }
            } catch (e: Exception) {
                showError(e.message ?: "Registration failed")
            } finally {
                btnRegister.isEnabled = true
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}