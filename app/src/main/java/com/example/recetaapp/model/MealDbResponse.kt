package com.example.recetaapp.model

data class MealDbResponse(
    val meals: List<Meal>
)

data class Meal(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String,
    val strInstructions: String,
    val strIngredient1: String?,
    val strIngredient2: String?,
    val strIngredient3: String?,

    val strMeasure1: String?,
    val strMeasure2: String?,
    val strMeasure3: String?

)


data class Ingredient(
    val ingredient: String,
    val quantity: String
)