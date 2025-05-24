package com.example.recetaapp.model

data class MealDbResponse(
    val meals: List<Meal>
)

data class Meal(
    val idMeal: String,  // Match with API field name
    val strMeal: String, // Title of the meal
    val strMealThumb: String,  // Image URL
    val strInstructions: String,  // Cooking instructions
    val strIngredient1: String?,  // First ingredient, continues up to strIngredient20
    val strIngredient2: String?,  // Second ingredient
    val strIngredient3: String?,
    // Add as many as needed, up to strIngredient20 in TheMealDB response
    val strMeasure1: String?,  // Quantity for the first ingredient
    val strMeasure2: String?,
    val strMeasure3: String?
    // Add as many as needed, up to strMeasure20 in TheMealDB response
)


data class Ingredient(
    val ingredient: String,
    val quantity: String
)