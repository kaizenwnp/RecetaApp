package com.example.recetaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.recetaapp.view.LoginActivity
import com.example.recetaapp.view.RandomMealActivity
import com.example.recetaapp.view.RecipeListActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.reflect.Field

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var progressBar: ProgressBar
    private lateinit var welcomeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()


        progressBar = findViewById(R.id.progressBar)
        welcomeText = findViewById(R.id.welcome_text)

        progressBar.max = 100
        progressBar.progress = 0


        CoroutineScope(Dispatchers.Main).launch {
            copyAllDrawableImagesToInternalStorage()
        }


        retrieveAndDisplayUserName()


        val goToRecipesButton: Button = findViewById(R.id.button_go_to_recipes)
        goToRecipesButton.setOnClickListener {
            val intent = Intent(this, RecipeListActivity::class.java)
            startActivity(intent)
        }


        val randomMealButton: Button = findViewById(R.id.button_random_meal)
        randomMealButton.setOnClickListener {
            val intent = Intent(this, RandomMealActivity::class.java)
            startActivity(intent)
        }


        val logoutButton: Button = findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {
            logout()
        }
    }


    private fun retrieveAndDisplayUserName() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val userRef = database.getReference("users").child(userId)

            userRef.child("name").get().addOnSuccessListener { dataSnapshot ->
                val userName = dataSnapshot.value as? String
                if (userName != null) {
                    welcomeText.text = "Welcome to Recipe App, $userName"
                } else {
                    welcomeText.text = "Welcome to Recipe App"
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load user name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }


    private suspend fun copyAllDrawableImagesToInternalStorage() = withContext(Dispatchers.IO) {
        val drawableClass = R.drawable::class.java
        val fields: Array<Field> = drawableClass.fields
        val totalImages = fields.size
        var currentProgress = 0

        for (field in fields) {
            try {
                val resourceId = field.getInt(null)
                val resourceName = field.name
                val fileName = "$resourceName.jpg"


                copyDrawableToInternalStorage(resourceId, fileName)


                currentProgress++
                withContext(Dispatchers.Main) {
                    val progress = (currentProgress.toFloat() / totalImages.toFloat() * 100).toInt()
                    progressBar.progress = progress
                }
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }


        withContext(Dispatchers.Main) {
            Toast.makeText(this@MainActivity, "All images copied!", Toast.LENGTH_SHORT).show()
            progressBar.progress = 100
        }
    }


    private fun copyDrawableToInternalStorage(drawableId: Int, fileName: String): String? {
        return try {
            val inputStream: InputStream = resources.openRawResource(drawableId)
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            file.name
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
