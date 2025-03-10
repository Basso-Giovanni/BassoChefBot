package com.example.bassochefbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.bassochefbot.ui.theme.BassoChefBotTheme
import com.example.bassochefbot.ui.theme.RecipeDetailsScreen
import com.example.bassochefbot.ui.theme.getIngredientsList
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = applicationContext
            val preferencesManager = remember { PreferencesManager(context) }
            val savedMeals = remember { mutableStateListOf<Meal>() }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    savedMeals.addAll(preferencesManager.getSavedRecipes())
                }
            }

            BassoChefBotTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(navController, savedMeals, preferencesManager)
                    }
                    composable("details/{mealId}") { backStackEntry ->
                        val mealId = backStackEntry.arguments?.getString("mealId")
                        mealId?.let {
                            RecipeDetailsScreen(mealId, navController, savedMeals, preferencesManager)
                        }
                    }
                    composable("savedRecipes") {
                        SavedRecipesScreen(savedMeals, navController)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    savedMeals: MutableList<Meal>,
    preferencesManager: PreferencesManager
) {
    // Stato per gestire la ricetta casuale
    val recipe = remember { mutableStateOf<Meal?>(null) }
    val error = remember { mutableStateOf<String?>(null) }
    val isLoading = remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // Stato per le animazioni durante lo swipe
    val isSwipeLoading = remember { mutableStateOf(false) }
    val swipeDirection = remember { mutableStateOf(0f) }

    // Funzione per caricare la ricetta randomica
    fun loadRandomRecipe() {
        isLoading.value = true
        error.value = null
        coroutineScope.launch {
            try {
                val response = RetrofitInstance.apiService.getRandomRecipe()
                if (response.isSuccessful && response.body()?.meals?.isNotEmpty() == true) {
                    recipe.value = response.body()?.meals?.first()
                } else {
                    error.value = "Error: get recipe"
                }
                isLoading.value = false
                isSwipeLoading.value = false
            } catch (e: Exception) {
                error.value = "Network error: ${e.localizedMessage}"
                isLoading.value = false
                isSwipeLoading.value = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadRandomRecipe()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.restaurant_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                            contentDescription = "Ristorante",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Basso ChefBot",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("savedRecipes") }) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Save recipes",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { loadRandomRecipe() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "New recipe")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { swipeDirection.value = 0f },
                        onDragEnd = {
                            // Carica nuova ricetta solo se lo swipe è stato verso l'alto di almeno 100 pixel
                            if (swipeDirection.value < -100f && !isLoading.value && !isSwipeLoading.value) {
                                isSwipeLoading.value = true
                                loadRandomRecipe()
                            }
                            swipeDirection.value = 0f
                        },
                        onDragCancel = { swipeDirection.value = 0f },
                        onDrag = { change, dragAmount ->
                            // Tiene traccia della direzione verticale dello swipe
                            swipeDirection.value += dragAmount.y
                            change.consume()
                        }
                    )
                }
        ) {
            if (isLoading.value) {
                LoadingIndicator()
            } else {
                error.value?.let {
                    ErrorMessage(message = it, onRetry = { loadRandomRecipe() })
                } ?: run {
                    recipe.value?.let { meal ->
                        AnimatedVisibility(
                            visible = !isLoading.value,
                            enter = fadeIn() + slideInVertically(
                                initialOffsetY = { 300 },
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
                            ),
                            exit = fadeOut() + slideOutVertically(
                                targetOffsetY = { 300 },
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
                            ),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                RecipeCard(
                                    meal = meal,
                                    navController = navController,
                                    savedMeals = savedMeals,
                                    preferencesManager = preferencesManager
                                )

                                // Indicatore di swipe
                                if (isSwipeLoading.value) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.padding(top = 16.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                // Istruzioni per l'utente
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                                    Text(
                                        text = "Swipe up for a new recipe\n" +
                                                "Double tap to save",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Making something delicious...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ErrorMessage(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.restaurant_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
            contentDescription = "Ristorante",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Retry")
        }
    }
}

@Composable
fun RecipeCard(
    meal: Meal,
    navController: NavHostController,
    savedMeals: MutableList<Meal>,
    preferencesManager: PreferencesManager? = null
) {
    val ingredientsList = getIngredientsList(meal)
    val isFavorite = remember { mutableStateOf(savedMeals.any { it.idMeal == meal.idMeal }) }
    val coroutineScope = rememberCoroutineScope()

    // Funzione per aggiungere/rimuovere dai preferiti
    fun toggleFavorite() {
        if (preferencesManager != null) {
            coroutineScope.launch {
                if (isFavorite.value) {
                    // Rimuovi dai preferiti
                    preferencesManager.removeRecipe(meal.idMeal)
                    savedMeals.removeIf { it.idMeal == meal.idMeal }
                } else {
                    // Aggiungi ai preferiti
                    preferencesManager.saveRecipe(meal)
                    if (!savedMeals.contains(meal)) {
                        savedMeals.add(meal)
                    }
                }
                isFavorite.value = !isFavorite.value
            }
        }
    }

    Card(
        modifier = Modifier
            .width(340.dp)
            .height(580.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .pointerInput(meal.idMeal) {
                detectTapGestures(
                    onDoubleTap = {
                        // Doppio tap per aggiungere/rimuovere dai preferiti
                        toggleFavorite()
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header con immagine e overlay per il titolo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // Immagine
                AsyncImage(
                    model = meal.strMealThumb,
                    contentDescription = "Recipe image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay sfumato per migliorare la leggibilità del titolo
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 50f
                            )
                        )
                )

                // Titolo in posizione
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        meal.strMeal,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Favoriti o categoria in un chip
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isFavorite.value) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (isFavorite.value) Color.Red else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                meal.strCategory ?: (if (isFavorite.value) "Favourite" else "Recipe"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Indicatore per informare che è una favorita
                if (isFavorite.value) {
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Red.copy(alpha = 0.9f),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Contenuto della card
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Sezione ingredienti con titolo evidenziato
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Main ingredients",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ingredientsList.take(5).forEach { ingredient ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    ingredient,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Piccolo estratto delle istruzioni
                Column {
                    Text(
                        "Instructions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        meal.strInstructions?.take(150) + "...",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Riga con pulsanti
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Pulsante preferiti (visibile solo sulla HomeScreen dove preferencesManager è disponibile)
                    if (preferencesManager != null) {
                        OutlinedButton(
                            onClick = { toggleFavorite() },
                            modifier = Modifier.weight(0.3f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isFavorite.value) Color.Red else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite.value) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favourite"
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Pulsante per visualizzare i dettagli
                    Button(
                        onClick = { navController.navigate("details/${meal.idMeal}") },
                        modifier = Modifier.weight(if (preferencesManager != null) 0.7f else 1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("View full recipe")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedRecipesScreen(savedMeals: List<Meal>, navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Le tue ricette salvate") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (savedMeals.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "You haven't saved any recipes yet",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Save your favorite recipes to find them easily in this section",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Scopri nuove ricette")
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(savedMeals, key = { meal -> meal.idMeal }) { meal ->
                        RecipeCard(meal, navController, savedMeals.toMutableList())
                    }
                }
            }
        }
    }
}

//se se lo sta chiedendo, l'app è in inglese perché i passaggi del MealDB sono in inglese, dunque non ha senso avere i comandi in italiano e i passggi in inglese :)