package com.example.recetaapp.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.recetaapp.MainActivity
import com.example.recetaapp.R
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val loginButton = findViewById<Button>(R.id.login_button)
        val registerButton = findViewById<Button>(R.id.register_button) // Register Button

        // Login Button Click Listener
        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Register Button Click Listener
        registerButton.setOnClickListener {
            // Navigate to the RegisterActivity when the Register button is clicked
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        checkLoggedInState()  // Check if user is already logged in
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Navigate to MainActivity after successful login
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()  // Close LoginActivity
                } else {
                    // Show error message on login failure
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkLoggedInState() {
        if (auth.currentUser != null) {
            // If user is already logged in, navigate to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()  // Close LoginActivity
        }
    }
}