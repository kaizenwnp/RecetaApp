package com.example.recetaapp.model

import java.io.Serializable

data class Recipe(
    val id: Int,
    val name: String,
    val type: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val imageUrl: String?,
) : Serializable
