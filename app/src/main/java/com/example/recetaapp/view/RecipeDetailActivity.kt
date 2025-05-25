package com.example.recetaapp.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.recetaapp.R
import com.example.recetaapp.model.Recipe
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var recipeImageView: ImageView
    private lateinit var recipeNameEditText: EditText
    private lateinit var recipeIngredientsEditText: EditText
    private lateinit var recipeStepsEditText: EditText
    private lateinit var recipeTypeSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var changeImageButton: Button

    private var recipeId: Int = -1
    private var imageUrl: String? = null
    private var selectedImageUri: Uri? = null


    private val recipeTypes = listOf("Soup", "Salad", "Dessert", "Main Course", "Appetizer")


    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let {
                recipeImageView.setImageURI(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        recipeImageView = findViewById(R.id.recipe_detail_image)
        recipeNameEditText = findViewById(R.id.recipe_detail_name)
        recipeIngredientsEditText = findViewById(R.id.recipe_detail_ingredients)
        recipeStepsEditText = findViewById(R.id.recipe_detail_steps)
        recipeTypeSpinner = findViewById(R.id.recipe_type_spinner)
        saveButton = findViewById(R.id.save_button)
        deleteButton = findViewById(R.id.delete_button)
        changeImageButton = findViewById(R.id.change_image_button)


        val recipe = intent.getSerializableExtra("RECIPE_DETAIL") as Recipe
        recipeId = recipe.id
        imageUrl = recipe.imageUrl


        recipeNameEditText.setText(recipe.name)
        recipeIngredientsEditText.setText(recipe.ingredients.joinToString(", "))
        recipeStepsEditText.setText(recipe.steps.joinToString(", "))


        setupRecipeTypeSpinner(recipe.type)


        loadRecipeImage()

        changeImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        saveButton.setOnClickListener {
            updateRecipe()
        }

        deleteButton.setOnClickListener {
            deleteRecipe()
        }
    }


    private fun setupRecipeTypeSpinner(selectedType: String) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, recipeTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        recipeTypeSpinner.adapter = adapter


        val spinnerPosition = recipeTypes.indexOf(selectedType)
        recipeTypeSpinner.setSelection(spinnerPosition)
    }


    private fun loadRecipeImage() {
        if (imageUrl != null && imageUrl!!.isNotBlank()) {
            val imageFile = File(filesDir, imageUrl!!)
            if (imageFile.exists()) {
                Glide.with(this)
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .into(recipeImageView)
            } else {
                recipeImageView.setImageResource(R.drawable.error)
            }
        } else {
            recipeImageView.setImageResource(R.drawable.error)
        }
    }


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

            file.name
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun updateRecipe() {
        val updatedName = recipeNameEditText.text.toString()
        val updatedIngredients = recipeIngredientsEditText.text.toString().split(",").map { it.trim() }
        val updatedSteps = recipeStepsEditText.text.toString().split(",").map { it.trim() }


        selectedImageUri?.let {
            imageUrl = saveImageToLocal(it)
        }


        val updatedType = recipeTypeSpinner.selectedItem.toString()


        val updatedRecipe = Recipe(
            id = recipeId,
            name = updatedName,
            type = updatedType,
            ingredients = updatedIngredients,
            steps = updatedSteps,
            imageUrl = imageUrl
        )


        val resultIntent = Intent()
        resultIntent.putExtra("UPDATED_RECIPE", updatedRecipe)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun deleteRecipe() {
        val resultIntent = Intent()
        resultIntent.putExtra("DELETE_RECIPE_ID", recipeId)
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}

