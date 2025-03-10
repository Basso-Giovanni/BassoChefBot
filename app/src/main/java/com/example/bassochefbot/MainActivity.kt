package com.example.bassochefbot

import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.bassochefbot.ui.theme.getIngredientsList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BassoChefBotTheme {
                // Configura il NavHost per gestire la navigazione
                val navController = rememberNavController()

                // Stato per memorizzare i preferiti
                val savedMeals = remember { mutableStateListOf<Meal>() }

                NavHost(navController = navController, startDestination = "home") {
                    // Aggiungi una destinazione per la schermata Home
                    composable("home") {
                        HomeScreen(navController = navController, savedMeals = savedMeals)
                    }
                    // Aggiungi una destinazione per i dettagli della ricetta
                    composable("details/{mealId}") { backStackEntry ->
                        val mealId = backStackEntry.arguments?.getString("mealId")
                        mealId?.let {
                            RecipeDetailsScreen(mealId = it, navController = navController, savedMeals = savedMeals)

                        }
                    }
                    // Aggiungi una destinazione per le ricette salvate
                    composable("savedRecipes") {
                        SavedRecipesScreen(savedMeals = savedMeals, navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController, savedMeals: MutableList<Meal>) {
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
                    RecipeCard(meal, navController, savedMeals)
                }
            }
        }

        // Bottone per navigare alle ricette salvate
        Button(
            onClick = { navController.navigate("savedRecipes") },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Mostra ricette salvate")
        }
    }
}


@Composable
fun RecipeCard(meal: Meal, navController: NavHostController, savedMeals: MutableList<Meal>) {
    val ingredientsList = getIngredientsList(meal)

    Card(
        modifier = Modifier
            .width(300.dp)  // Larghezza fissa
            .height(700.dp) // Altezza fissa
            .padding(16.dp)
            .clickable { navController.navigate("details/${meal.idMeal}") }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                meal.strMeal,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Image(
                painter = rememberImagePainter(meal.strMealThumb),
                contentDescription = "Recipe image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp) // Imposta una dimensione fissa per l'immagine
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("Ingredients:", style = MaterialTheme.typography.bodyMedium)
            ingredientsList.take(3).forEach { ingredient -> // Mostra solo i primi 3 ingredienti per evitare overflow
                Text("• $ingredient", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Instructions:", style = MaterialTheme.typography.bodyMedium)
            Text(
                meal.strInstructions?.take(300) + "...", // Accorcia il testo se necessario
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}



@Composable
fun SavedRecipesScreen(savedMeals: List<Meal>, navController: NavHostController) {
    // Schermata che mostra tutte le ricette salvate
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Aggiungi il bottone di ritorno
        IconButton(
            onClick = { navController.popBackStack() }, // Torna alla schermata precedente
            modifier = Modifier.padding(start = 8.dp, top = 8.dp) // Spazio intorno al bottone
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
        }

        // Titolo della schermata
        Text(
            text = "Ricette Salvate",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Se non ci sono ricette salvate, mostra un messaggio
        if (savedMeals.isEmpty()) {
            Text(text = "Nessuna ricetta salvata", style = MaterialTheme.typography.bodyLarge)
        } else {
            // Mostra ogni ricetta salvata come una card
            LazyColumn {
                items(savedMeals, key = { meal -> meal.idMeal }) { meal ->
                    // Per ogni ricetta salvata, mostra una RecipeCard
                    RecipeCard(meal, navController, savedMeals.toMutableList())
                }
            }
        }
    }
}
