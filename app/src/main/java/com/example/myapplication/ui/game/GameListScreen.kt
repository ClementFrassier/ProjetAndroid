package com.example.myapplication.ui.game

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.GameWithEditor
import com.example.myapplication.ui.viewmodel.AuthViewModel
import com.example.myapplication.ui.viewmodel.EditorViewModel
import com.example.myapplication.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameListScreen(
    viewModel: GameViewModel,
    authViewModel: AuthViewModel,
    editorViewModel: EditorViewModel,
    onGameClick: (Int) -> Unit,
    onCreateGame: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.listState.collectAsState()
    val editorState by editorViewModel.listState.collectAsState()
    val canManageGames = authViewModel.canManageFestivals()

    var searchQuery by remember { mutableStateOf("") }
    var selectedEditorId by remember { mutableStateOf<Int?>(null) }
    var selectedType by remember { mutableStateOf("") }
    var selectedSort by remember { mutableStateOf("nom") }
    
    var showFilters by remember { mutableStateOf(false) }
    var expandedEditor by remember { mutableStateOf(false) }
    var expandedSort by remember { mutableStateOf(false) }

    fun refreshGames() {
        viewModel.loadGames(
            editorId = selectedEditorId,
            query = searchQuery.takeIf { it.isNotBlank() },
            type = selectedType.takeIf { it.isNotBlank() },
            sort = selectedSort
        )
    }

    LaunchedEffect(Unit) {
        refreshGames()
        editorViewModel.loadEditors()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jeux", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp)
                    }
                }
            )
        },
        floatingActionButton = {
            if (canManageGames) {
                FloatingActionButton(onClick = onCreateGame) {
                    Icon(Icons.Filled.Add, contentDescription = "Créer un Jeu")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    refreshGames()
                },
                placeholder = { Text("Rechercher un jeu (nom ou auteur)...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Rechercher") },
                trailingIcon = {
                    Row {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { 
                                searchQuery = ""
                                refreshGames()
                            }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Effacer")
                            }
                        }
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(Icons.Filled.FilterList, contentDescription = "Filtres", tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            // Extensible Filters
            if (showFilters) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        
                        // Editor Filter
                        ExposedDropdownMenuBox(
                            expanded = expandedEditor,
                            onExpandedChange = { expandedEditor = !expandedEditor }
                        ) {
                            OutlinedTextField(
                                value = editorState.editors.find { it.id == selectedEditorId }?.name ?: "Tous les éditeurs",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Éditeur") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEditor) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedEditor,
                                onDismissRequest = { expandedEditor = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Tous les éditeurs") },
                                    onClick = {
                                        selectedEditorId = null
                                        expandedEditor = false
                                        refreshGames()
                                    }
                                )
                                editorState.editors.forEach { editor ->
                                    DropdownMenuItem(
                                        text = { Text(editor.name) },
                                        onClick = {
                                            selectedEditorId = editor.id
                                            expandedEditor = false
                                            refreshGames()
                                        }
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Type Filter
                            OutlinedTextField(
                                value = selectedType,
                                onValueChange = { 
                                    selectedType = it
                                    refreshGames()
                                },
                                label = { Text("Type / Catégorie") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            // Sort Option
                            ExposedDropdownMenuBox(
                                expanded = expandedSort,
                                onExpandedChange = { expandedSort = !expandedSort },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = when (selectedSort) {
                                        "nom" -> "Nom"
                                        "editeur" -> "Éditeur"
                                        "type" -> "Type"
                                        else -> "Nom"
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Trier par") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSort) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedSort,
                                    onDismissRequest = { expandedSort = false }
                                ) {
                                    listOf("nom" to "Nom", "editeur" to "Éditeur", "type" to "Type").forEach { (key, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                selectedSort = key
                                                expandedSort = false
                                                refreshGames()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading && state.games.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    state.errorMessage != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "⚠️ ${state.errorMessage}",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { refreshGames() }) {
                                Text("Réessayer")
                            }
                        }
                    }
                    state.games.isEmpty() -> {
                        Text(
                            "Aucun jeu trouvé",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.games) { game ->
                                GameCard(
                                    game = game,
                                    onClick = { onGameClick(game.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameCard(game: GameWithEditor, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Casino,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = game.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    game.editorName?.let {
                        Text(
                            text = "Éditeur : $it",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!game.type.isNullOrBlank()) {
                    AssistChip(
                        onClick = {},
                        label = { Text(game.type) }
                    )
                }
                if (game.minAge != null || game.maxAge != null) {
                    val ageText = when {
                        game.minAge != null && game.maxAge != null -> "${game.minAge} - ${game.maxAge} ans"
                        game.minAge != null -> "Dès ${game.minAge} ans"
                        else -> "Jusqu'à ${game.maxAge} ans"
                    }
                    AssistChip(
                        onClick = {},
                        label = { Text(ageText) }
                    )
                }
            }
        }
    }
}
