package com.example.recetaapp.view

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.recetaapp.R
import com.example.recetaapp.model.Meal
import com.example.recetaapp.model.MealDbResponse
import com.example.recetaapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RandomMealActivity : AppCompatActivity() {

    private lateinit var mealImage: ImageView
    private lateinit var mealName: TextView
    private lateinit var mealIngredients: TextView
    private lateinit var mealInstructions: TextView
    private lateinit var refreshButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_random_meal)

        mealImage = findViewById(R.id.meal_image)
        mealName = findViewById(R.id.meal_name)
        mealIngredients = findViewById(R.id.meal_ingredients)
        mealInstructions = findViewById(R.id.meal_instructions)
        refreshButton = findViewById(R.id.refresh_button)

        // Fetch and display random meal on activity launch
        fetchRandomMeal()

        // Add listener to refresh button to fetch another random meal
        refreshButton.setOnClickListener {
            fetchRandomMeal()
        }
    }

    private fun fetchRandomMeal() {
        val call = RetrofitClient.api.getRandomMeal()

        // Make the API call
        call.enqueue(object : Callback<MealDbResponse> {
            override fun onResponse(call: Call<MealDbResponse>, response: Response<MealDbResponse>) {
                // Check if response is successful and not null
                if (response.isSuccessful && response.body() != null) {
                    val mealList = response.body()?.meals
                    if (mealList != null && mealList.isNotEmpty()) {
                        // Update the UI on the main thread
                        runOnUiThread {
                            displayMeal(mealList[0])  // Display the first (and only) meal
                        }
                    } else {
                        // Log an error if no meals are returned
                        Toast.makeText(this@RandomMealActivity, "No meal found!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle failed responses or empty body
                    Toast.makeText(this@RandomMealActivity, "Failed to fetch meal. Response: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MealDbResponse>, t: Throwable) {
                // Handle network or other errors
                runOnUiThread {
                    Toast.makeText(this@RandomMealActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


    private fun displayMeal(meal: Meal) {
        mealName.text = meal.strMeal
        Glide.with(this).load(meal.strMealThumb).into(mealImage)

        // Gather ingredients and measurements manually from the individual fields
        val ingredientsList = mutableListOf<String>()

        // Add ingredients and quantities only if they're not null or empty
        meal.strIngredient1?.let { if (it.isNotBlank()) ingredientsList.add("${meal.strMeasure1 ?: ""} $it") }
        meal.strIngredient2?.let { if (it.isNotBlank()) ingredientsList.add("${meal.strMeasure2 ?: ""} $it") }
        meal.strIngredient3?.let { if (it.isNotBlank()) ingredientsList.add("${meal.strMeasure3 ?: ""} $it") }
        // Continue adding ingredients from strIngredient4 to strIngredient20...

        // Join the ingredients into a single string
        mealIngredients.text = "Ingredients: ${ingredientsList.joinToString(separator = ", ")}"
        mealInstructions.text = meal.strInstructions
    }

}
