package com.example.bassochefbot

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MealApiService {

    @GET("api/json/v1/1/random.php")
    suspend fun getRandomRecipe(): Response<RecipeResponse>

    @GET("api/json/v1/1/search.php")
    suspend fun searchRecipe(@Query("s") recipeName: String): Response<RecipeResponse>

    @GET("api/json/v1/1/lookup.php")
    suspend fun getRecipeDetails(@Query("i") mealId: String): Response<RecipeResponse>
}
