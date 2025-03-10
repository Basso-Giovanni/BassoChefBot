package com.example.bassochefbot.ui.theme

import android.os.Bundle
import android.speech.tts.TextToSpeech
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.example.bassochefbot.Meal
import com.example.bassochefbot.PreferencesManager
import com.example.bassochefbot.RetrofitInstance
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun RecipeDetailsScreen(
    mealId: String,
    navController: NavController,
    savedMeals: MutableList<Meal>,
    preferencesManager: PreferencesManager? = null
) {
    val recipe = remember { mutableStateOf<Meal?>(null) }
    val isFavorite = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val textToSpeech = remember { mutableStateOf<TextToSpeech?>(null) }

    // Inizializzazione del Text-to-Speech
    LaunchedEffect(Unit) {
        textToSpeech.value = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.value?.language = Locale.ENGLISH
            } else {
                Log.e("TextToSpeech", "Initialization failed")
            }
        }
    }

    // Funzione per avviare la lettura vocale
    fun speak(text: String) {
        textToSpeech.value?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // Funzione per aggiungere/rimuovere dai preferiti
    fun toggleFavorite(meal: Meal) {
        coroutineScope.launch {
            if (isFavorite.value) {
                savedMeals.remove(meal)
            } else {
                savedMeals.add(meal)
            }
            preferencesManager?.saveRecipes(savedMeals)
            isFavorite.value = !isFavorite.value
        }
    }

    // Caricamento della ricetta
    LaunchedEffect(mealId) {
        recipe.value = RetrofitInstance.apiService.getRecipeDetails(mealId).body()?.meals?.firstOrNull()
        isFavorite.value = savedMeals.any { it.idMeal == mealId }
    }

    // Layout con scrolling
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        // Bottone per tornare indietro
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        recipe.value?.let { meal ->
            // Titolo della ricetta
            Text(meal.strMeal, style = MaterialTheme.typography.headlineMedium)

            // Immagine della ricetta
            AsyncImage(
                model = meal.strMealThumb,
                contentDescription = "Recipe image",
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bottone per aggiungere/rimuovere dai preferiti
            Button(onClick = { toggleFavorite(meal) }) {
                Text(if (isFavorite.value) "Rimuovi dai preferiti" else "Aggiungi ai preferiti")
            }

            // Sezione degli ingredienti
            Spacer(modifier = Modifier.height(16.dp))
            Text("Ingredients:", style = MaterialTheme.typography.bodyMedium)
            val ingredientsList = getIngredientsList(meal)
            ingredientsList.forEach { ingredient ->
                Text("• $ingredient", style = MaterialTheme.typography.bodySmall)
            }

            // Bottone per leggere gli ingredienti
            Button(onClick = {
                val ingredientsText = ingredientsList.joinToString(separator = ", ")
                speak("Ingredienti: $ingredientsText")
            }) {
                Text("Leggi gli ingredienti")
            }

            // Sezione delle istruzioni
            Spacer(modifier = Modifier.height(16.dp))
            Text("Instructions:", style = MaterialTheme.typography.bodyMedium)
            Text(meal.strInstructions ?: "No instructions available", style = MaterialTheme.typography.bodySmall)

            // Bottone per leggere le istruzioni
            Button(onClick = {
                val instructionsText = meal.strInstructions ?: "No instructions available"
                speak(instructionsText)
            }) {
                Text("Leggi le istruzioni")
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
