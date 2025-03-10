package com.example.bassochefbot.ui.theme

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bassochefbot.Meal
import com.example.bassochefbot.PreferencesManager
import com.example.bassochefbot.RetrofitInstance
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore


@OptIn(ExperimentalMaterial3Api::class)
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
    val scrollState = rememberScrollState()
    val showIngredients = remember { mutableStateOf(true) }
    val showInstructions = remember { mutableStateOf(true) }

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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Dettagli Ricetta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            recipe.value?.let { meal ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Immagine della ricetta con overlay e pulsante preferiti
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    ) {
                        AsyncImage(
                            model = meal.strMealThumb,
                            contentDescription = "Recipe image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Overlay scuro graduale in basso per migliorare la leggibilità del testo
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.7f)
                                        ),
                                        startY = 150f
                                    )
                                )
                        )

                        // Pulsante per aggiungere/rimuovere dai preferiti
                        IconButton(
                            onClick = { toggleFavorite(meal) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                if (isFavorite.value) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Titolo della ricetta posizionato in basso
                        Text(
                            text = meal.strMeal,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        )
                    }

                    // Card per gli ingredienti
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .animateContentSize()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Ingredienti",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { showIngredients.value = !showIngredients.value }) {
                                    Icon(
                                        if (showIngredients.value) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle ingredients"
                                    )
                                }
                            }

                            if (showIngredients.value) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))

                                val ingredientsList = getIngredientsList(meal)
                                ingredientsList.forEach { ingredient ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            ingredient,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        val ingredientsText = ingredientsList.joinToString(separator = ", ")
                                        speak("Ingredienti: $ingredientsText")
                                    },
                                    modifier = Modifier.align(Alignment.End),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Leggi ingredienti")
                                    }
                                }
                            }
                        }
                    }

                    // Card per le istruzioni
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .animateContentSize()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Istruzioni",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { showInstructions.value = !showInstructions.value }) {
                                    Icon(
                                        if (showInstructions.value) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle instructions"
                                    )
                                }
                            }

                            if (showInstructions.value) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))

                                Text(
                                    meal.strInstructions ?: "Nessuna istruzione disponibile",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                Button(
                                    onClick = {
                                        val instructionsText = meal.strInstructions ?: "Nessuna istruzione disponibile"
                                        speak(instructionsText)
                                    },
                                    modifier = Modifier.align(Alignment.End),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Leggi istruzioni")
                                    }
                                }
                            }
                        }
                    }

                    // Informazioni aggiuntive (questo potrebbe contenere categoria, area, tag, ecc.)
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Informazioni aggiuntive",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    "Categoria: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    meal.strCategory ?: "N/A",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    "Area: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    meal.strArea ?: "N/A",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            if (!meal.strTags.isNullOrBlank()) {
                                Text(
                                    "Tags:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    maxItemsInEachRow = 3
                                ) {
                                    meal.strTags.split(",").forEach { tag ->
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                                        ) {
                                            Text(
                                                tag.trim(),
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            } ?: run {
                // Schermata di caricamento
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

// FlowRow custom component per disporre i tag
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val rows = mutableListOf<MutableList<androidx.compose.ui.layout.Placeable>>()
        val rowConstraints = constraints.copy(minWidth = 0)

        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0
        var currentRowItemCount = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(rowConstraints)

            if (currentRowItemCount >= maxItemsInEachRow || currentRowWidth + placeable.width > constraints.maxWidth) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentRowWidth = 0
                currentRowItemCount = 0
            }

            currentRow.add(placeable)
            currentRowWidth += placeable.width
            currentRowItemCount++
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        val height = rows.sumOf { row -> row.maxOfOrNull { it.height } ?: 0 }

        layout(constraints.maxWidth, height) {
            var y = 0

            rows.forEach { row ->
                val rowWidth = row.sumOf { it.width }
                val horizontalGap = when {
                    row.size <= 1 -> 0
                    else -> (constraints.maxWidth - rowWidth) / (row.size - 1)
                }

                var x = when (horizontalArrangement) {
                    Arrangement.Start -> 0
                    Arrangement.End -> constraints.maxWidth - rowWidth
                    Arrangement.Center -> (constraints.maxWidth - rowWidth) / 2
                    Arrangement.SpaceBetween -> 0
                    else -> 0
                }

                row.forEach { placeable ->
                    placeable.place(x, y)
                    x += placeable.width + horizontalGap
                }

                y += row.maxOfOrNull { it.height } ?: 0
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