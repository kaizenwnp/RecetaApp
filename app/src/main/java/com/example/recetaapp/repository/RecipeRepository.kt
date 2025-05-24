package com.example.recetaapp.repository

import android.content.Context
import com.example.recetaapp.model.Recipe
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class RecipeRepository(private val context: Context) {

    fun getRecipesFromJson(): List<Recipe> {
        val json: String?
        try {
            val inputStream = context.assets.open("recipes.json")
            json = inputStream.bufferedReader().use { it.readText() }
        } catch (ex: IOException) {
            ex.printStackTrace()
            return emptyList()
        }

        val recipeType = object : TypeToken<List<Recipe>>() {}.type
        return Gson().fromJson(json, recipeType)
    }
}