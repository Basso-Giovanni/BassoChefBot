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

    suspend fun saveRecipes(recipes: List<Meal>) {
        val json = gson.toJson(recipes)
        context.dataStore.edit { preferences ->
            preferences[KEY_SAVED_RECIPES] = json
        }
    }

    suspend fun getSavedRecipes(): List<Meal> {
        val json = context.dataStore.data
            .map { preferences -> preferences[KEY_SAVED_RECIPES] ?: "[]" }
            .first()

        return gson.fromJson(json, Array<Meal>::class.java).toList()
    }
}
