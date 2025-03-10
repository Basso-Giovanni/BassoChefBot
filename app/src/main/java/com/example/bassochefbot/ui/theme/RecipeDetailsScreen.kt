package com.example.bassochefbot.ui.theme

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.bassochefbot.Meal
import com.example.bassochefbot.RetrofitInstance
import kotlinx.coroutines.launch

@Composable
fun RecipeDetailsScreen(mealId: String, navController: NavController) {
    val recipe = remember { mutableStateOf<Meal?>(null) }
    val isLoading = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun loadRecipeDetails() {
        isLoading.value = true
        error.value = null
        coroutineScope.launch {
            try {
                val response = RetrofitInstance.apiService.getRecipeDetails(mealId)

                if (response.isSuccessful && response.body()?.meals?.isNotEmpty() == true) {
                    recipe.value = response.body()?.meals?.first()
                    isLoading.value = false
                } else {
                    error.value = "Errore nel recupero dei dettagli"
                    isLoading.value = false
                }
            } catch (e: Exception) {
                error.value = "Errore di rete: ${e.localizedMessage}"
                isLoading.value = false
            }
        }
    }

    LaunchedEffect(mealId) {
        loadRecipeDetails()
    }

    // UI con LazyColumn per la parte scorrevole
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())  // Aggiungi il modificatore per lo scrolling
    ) {
        // Aggiungi il bottone di ritorno
        IconButton(
            onClick = {
                navController.popBackStack() // Torna indietro alla schermata precedente
            }
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        if (isLoading.value) {
            CircularProgressIndicator()
        }

        error.value?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        recipe.value?.let { meal ->
            Text(meal.strMeal, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Image(painter = rememberImagePainter(meal.strMealThumb), contentDescription = "Recipe image")
            Spacer(modifier = Modifier.height(8.dp))

            Text("Ingredients:", style = MaterialTheme.typography.bodySmall)
            meal.strIngredients?.let { Text(it) }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Instructions:", style = MaterialTheme.typography.bodySmall)
            Text(meal.strInstructions ?: "No instructions available")
        }
    }
}
