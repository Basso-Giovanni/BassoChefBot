package com.example.bassochefbot.ui.theme

import android.os.Bundle
import android.util.Log
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
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.example.bassochefbot.Meal
import com.example.bassochefbot.RetrofitInstance
import kotlinx.coroutines.launch

@Composable
fun RecipeDetailsScreen(mealId: String, navController: NavController, savedMeals: MutableList<Meal>) {
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
                    Log.d("RecipeDetailsScreen", "Recipe loaded: ${recipe.value}")
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Bottone per tornare indietro
        IconButton(
            onClick = { navController.popBackStack() }
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        if (isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (error.value != null) {
            Text(text = error.value!!, color = MaterialTheme.colorScheme.error)
        } else {
            recipe.value?.let { meal ->
                val isFavorite = remember { mutableStateOf(savedMeals.contains(meal)) }

                Text(meal.strMeal, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))

                // Caricamento dell'immagine
                AsyncImage(
                    model = meal.strMealThumb,
                    contentDescription = "Recipe image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Bottone per aggiungere/rimuovere dai preferiti
                Button(
                    onClick = {
                        if (isFavorite.value) {
                            savedMeals.remove(meal)  // Rimuove la ricetta dai preferiti
                        } else {
                            savedMeals.add(meal)  // Aggiunge la ricetta ai preferiti
                        }
                        isFavorite.value = !isFavorite.value  // Aggiorna lo stato
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isFavorite.value) "Rimuovi dai preferiti" else "Aggiungi ai preferiti")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Lista degli ingredienti
                Text("Ingredients:", style = MaterialTheme.typography.bodySmall)

                val ingredientsList = getIngredientsList(meal)
                ingredientsList.forEach { ingredient ->
                    Text("• $ingredient", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Instructions:", style = MaterialTheme.typography.bodySmall)
                Text(meal.strInstructions ?: "No instructions available")
            }
        }
    }
}


fun getIngredientsList(meal: Meal): List<String> {
    val ingredients = mutableListOf<String>()

    val ingredientFields = listOf(
        meal.strIngredient1 to meal.strMeasure1,
        meal.strIngredient2 to meal.strMeasure2,
        meal.strIngredient3 to meal.strMeasure3,
        meal.strIngredient4 to meal.strMeasure4,
        meal.strIngredient5 to meal.strMeasure5,
        meal.strIngredient6 to meal.strMeasure6,
        meal.strIngredient7 to meal.strMeasure7,
        meal.strIngredient8 to meal.strMeasure8,
        meal.strIngredient9 to meal.strMeasure9,
        meal.strIngredient10 to meal.strMeasure10,
        meal.strIngredient11 to meal.strMeasure11,
        meal.strIngredient12 to meal.strMeasure12,
        meal.strIngredient13 to meal.strMeasure13,
        meal.strIngredient14 to meal.strMeasure14,
        meal.strIngredient15 to meal.strMeasure15,
        meal.strIngredient16 to meal.strMeasure16,
        meal.strIngredient17 to meal.strMeasure17,
        meal.strIngredient18 to meal.strMeasure18,
        meal.strIngredient19 to meal.strMeasure19,
        meal.strIngredient20 to meal.strMeasure20
    )

    for ((ingredient, measure) in ingredientFields) {
        if (!ingredient.isNullOrBlank()) {
            ingredients.add("$ingredient - ${measure ?: ""}".trim())
        }
    }

    return ingredients
}
