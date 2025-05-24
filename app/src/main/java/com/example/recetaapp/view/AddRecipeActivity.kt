package com.example.recetaapp.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.recetaapp.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var recipeNameInput: EditText
    private lateinit var recipeTypeSpinner: Spinner
    private lateinit var addImageButton: Button
    private lateinit var selectedImageView: ImageView
    private lateinit var recipeIngredientsInput: EditText
    private lateinit var recipeStepsInput: EditText
    private lateinit var submitRecipeButton: Button

    private var selectedImageUri: Uri? = null

    // Register for image picking result
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            selectedImageUri = result.data?.data
            selectedImageView.setImageURI(selectedImageUri)
            selectedImageView.visibility = ImageView.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        // Initialize views
        recipeNameInput = findViewById(R.id.recipe_name_input)
        recipeTypeSpinner = findViewById(R.id.recipe_type_spinner)
        addImageButton = findViewById(R.id.add_image_button)
        selectedImageView = findViewById(R.id.selected_image)
        recipeIngredientsInput = findViewById(R.id.recipe_ingredients_input)
        recipeStepsInput = findViewById(R.id.recipe_steps_input)
        submitRecipeButton = findViewById(R.id.submit_recipe_button)

        // Load the recipe types from the intent and set to the spinner
        val recipeTypes = intent.getStringArrayListExtra("RECIPE_TYPES")
        if (recipeTypes != null) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, recipeTypes)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            recipeTypeSpinner.adapter = adapter
        }

        // Handle image selection
        addImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        // Handle recipe submission
        submitRecipeButton.setOnClickListener {
            val recipeName = recipeNameInput.text.toString()
            val recipeType = recipeTypeSpinner.selectedItem?.toString() ?: ""
            val recipeIngredients = recipeIngredientsInput.text.toString().split(",").map { it.trim() }
            val recipeSteps = recipeStepsInput.text.toString().split(",").map { it.trim() }

            // Validate inputs
            if (recipeName.isBlank() || recipeType.isBlank() || recipeIngredients.isEmpty() || recipeSteps.isEmpty()) {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save the image locally and then submit the recipe
            selectedImageUri?.let {
                val localImagePath = saveImageToLocal(it)
                submitRecipe(recipeName, recipeType, recipeIngredients, recipeSteps, localImagePath)
            } ?: submitRecipe(recipeName, recipeType, recipeIngredients, recipeSteps, null)
        }
    }

    // Function to save the selected image to internal storage
    private fun saveImageToLocal(imageUri: Uri): String? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            val file = File(filesDir, "recipe_${UUID.randomUUID()}.jpg")
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            file.name  // Return the name of the saved image
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Function to submit the recipe
    private fun submitRecipe(name: String, type: String, ingredients: List<String>, steps: List<String>, imagePath: String?) {
        val resultIntent = Intent().apply {
            putExtra("NEW_RECIPE_NAME", name)
            putExtra("NEW_RECIPE_TYPE", type)
            putExtra("NEW_RECIPE_INGREDIENTS", ArrayList(ingredients))
            putExtra("NEW_RECIPE_STEPS", ArrayList(steps))
            putExtra("NEW_RECIPE_IMAGE_URL", imagePath)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish() // Close the activity after submission
    }
}