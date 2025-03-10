package com.example.bassochefbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import coil.compose.rememberImagePainter
import com.example.bassochefbot.ui.theme.BassoChefBotTheme
import com.example.bassochefbot.ui.theme.RecipeDetailsScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BassoChefBotTheme {
                // Configura il NavHost per gestire la navigazione
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {
                    // Aggiungi una destinazione per la schermata Home
                    composable("home") {
                        HomeScreen(navController = navController)
                    }
                    // Aggiungi una destinazione per i dettagli della ricetta
                    composable("details/{mealId}") { backStackEntry ->
                        val mealId = backStackEntry.arguments?.getString("mealId")
                        mealId?.let {
                            RecipeDetailsScreen(mealId = it, navController = navController)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun HomeScreen(navController: NavHostController) {
    // Stato per gestire la ricetta casuale
    val recipe = remember { mutableStateOf<Meal?>(null) }
    val error = remember { mutableStateOf<String?>(null) }
    val isLoading = remember { mutableStateOf(true) }

    // Coroutine per caricare una ricetta casuale
    val coroutineScope = rememberCoroutineScope()

    // Funzione per caricare la ricetta randomica
    fun loadRandomRecipe() {
        isLoading.value = true
        error.value = null
        coroutineScope.launch {
            try {
                // Chiamata all'API per ottenere una ricetta casuale
                val response = RetrofitInstance.apiService.getRandomRecipe()

                if (response.isSuccessful && response.body()?.meals?.isNotEmpty() == true) {
                    recipe.value = response.body()?.meals?.first()
                    isLoading.value = false
                } else {
                    error.value = "Errore nel recupero della ricetta"
                    isLoading.value = false
                }
            } catch (e: Exception) {
                error.value = "Errore di rete: ${e.localizedMessage}"
                isLoading.value = false
            }
        }
    }

    // Carica la prima ricetta quando la schermata viene mostrata
    LaunchedEffect(Unit) {
        loadRandomRecipe()
    }

    // UI da mostrare: caricamento, errore o ricetta
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -200) {  // Se lo swipe è abbastanza lungo verso l'alto
                        loadRandomRecipe()  // Carica una nuova ricetta
                    }
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally, // Centra orizzontalmente
        verticalArrangement = Arrangement.spacedBy(16.dp) // Spaziatura tra gli elementi
    ) {
        // Titolo dell'app
        Text(
            text = "BassoChefBot",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 32.dp) // Un po' di spazio sopra
        )

        // Indicatore di caricamento
        if (isLoading.value) {
            CircularProgressIndicator()
        } else {
            // Se c'è un errore, visualizza il messaggio di errore
            error.value?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            // Mostra la ricetta se è stata trovata
            recipe.value?.let { meal ->
                AnimatedVisibility(
                    visible = !isLoading.value,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -40 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { 40 }),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RecipeCard(meal, navController)
                }
            }
        }
    }
}

@Composable
fun RecipeCard(meal: Meal, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                navController.navigate("details/${meal.idMeal}")
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Titolo della ricetta (centrato orizzontalmente)
            Text(
                meal.strMeal,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Immagine della ricetta (centrata orizzontalmente)
            Image(
                painter = rememberImagePainter(meal.strMealThumb),
                contentDescription = "Recipe image",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Mostra gli ingredienti
            meal.strIngredients?.let {
                Text(
                    "Ingredients:",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Anteprima delle istruzioni (centrata orizzontalmente)
            Text(
                "Instructions:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                meal.strInstructions.take(100) + "...",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
