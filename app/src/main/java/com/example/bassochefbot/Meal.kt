package com.example.bassochefbot

data class RecipeResponse(
    val meals: List<Meal>
)

data class Meal(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String,
    val strInstructions: String,
    val strIngredients: String? // Può essere null o una stringa
)
