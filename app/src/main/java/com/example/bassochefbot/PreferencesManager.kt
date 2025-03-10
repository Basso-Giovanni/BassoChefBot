package com.example.bassochefbot

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "saved_recipes")

class PreferencesManager(private val context: Context) {
    private val gson = Gson()
    private val KEY_SAVED_RECIPES = stringPreferencesKey("saved_recipes")

    /**
     * Salva una lista completa di ricette
     */
    suspend fun saveRecipes(recipes: List<Meal>) {
        val json = gson.toJson(recipes)
        context.dataStore.edit { preferences ->
            preferences[KEY_SAVED_RECIPES] = json
        }
    }

    /**
     * Ottiene tutte le ricette salvate
     */
    suspend fun getSavedRecipes(): List<Meal> {
        val json = context.dataStore.data
            .map { preferences -> preferences[KEY_SAVED_RECIPES] ?: "[]" }
            .first()

        return gson.fromJson(json, Array<Meal>::class.java)?.toList() ?: emptyList()
    }

    /**
     * Salva una singola ricetta nei preferiti
     */
    suspend fun saveRecipe(meal: Meal) {
        val currentRecipes = getSavedRecipes().toMutableList()

        // Verifica se la ricetta esiste già
        if (currentRecipes.none { it.idMeal == meal.idMeal }) {
            currentRecipes.add(meal)
            saveRecipes(currentRecipes)
        }
    }

    /**
     * Rimuove una ricetta dai preferiti
     */
    suspend fun removeRecipe(mealId: String) {
        val currentRecipes = getSavedRecipes().toMutableList()
        currentRecipes.removeIf { it.idMeal == mealId }
        saveRecipes(currentRecipes)
    }

    /**
     * Verifica se una ricetta è salvata nei preferiti
     */
    suspend fun isSaved(mealId: String): Boolean {
        val currentRecipes = getSavedRecipes()
        return currentRecipes.any { it.idMeal == mealId }
    }
}