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

class LoginActivity : AppCompatActivity() {
    
    private lateinit var apiClient: ApiClient
    private lateinit var preferenceManager: PreferenceManager
    
    private lateinit var etServerUrl: TextInputEditText
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var progressBar: View
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        apiClient = ApiClient(this)
        preferenceManager = PreferenceManager(this)
        
        // Check if already logged in
        if (preferenceManager.isLoggedIn) {
            startMainActivity()
            return
        }
        
        initViews()
        setupClickListeners()
    }
    
    private fun initViews() {
        etServerUrl = findViewById(R.id.etServerUrl)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)
        
        // Pre-fill server URL
        etServerUrl.setText(preferenceManager.serverUrl)
    }
    
    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val serverUrl = etServerUrl.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            
            if (validateInput(serverUrl, username, password)) {
                performLogin(serverUrl, username, password)
            }
        }
        
        findViewById<View>(R.id.tvRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
    
    private fun validateInput(serverUrl: String, username: String, password: String): Boolean {
        when {
            serverUrl.isEmpty() -> {
                etServerUrl.error = "Server URL is required"
                return false
            }
            username.isEmpty() -> {
                etUsername.error = getString(R.string.username_required)
                return false
            }
            password.isEmpty() -> {
                etPassword.error = getString(R.string.password_required)
                return false
            }
        }
        return true
    }
    
    private fun performLogin(serverUrl: String, username: String, password: String) {
        btnLogin.isEnabled = false
        progressBar.visibility = View.VISIBLE
        
        // Save server URL
        preferenceManager.serverUrl = serverUrl
        
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    apiClient.login(username, password)
                }
                
                if (result.isSuccess) {
                    startMainActivity()
                } else {
                    showError(result.exceptionOrNull()?.message ?: "Login failed")
                }
            } catch (e: Exception) {
                showError(e.message ?: "Login failed")
            } finally {
                btnLogin.isEnabled = true
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}