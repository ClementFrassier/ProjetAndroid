package com.example.myapplication.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.GameCreateInput
import com.example.myapplication.model.GameInput
import com.example.myapplication.ui.viewmodel.AuthViewModel
import com.example.myapplication.ui.viewmodel.EditorViewModel
import com.example.myapplication.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    gameId: Int?, 
    viewModel: GameViewModel,
    authViewModel: AuthViewModel,
    editorViewModel: EditorViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.detailState.collectAsState()
    val editorState by editorViewModel.listState.collectAsState()
    val canManageGames = authViewModel.canManageFestivals()
    
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var minAge by remember { mutableStateOf("") }
    var maxAge by remember { mutableStateOf("") }
    
    // Pour la création uniquement
    var selectedEditorId by remember { mutableStateOf<Int?>(null) }
    var expandedEditorDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (gameId == null) {
            editorViewModel.loadEditors() // Charger les éditeurs pour la création
        }
    }

    LaunchedEffect(gameId) {
        if (gameId != null) {
            viewModel.loadGame(gameId)
        } else {
            viewModel.resetDetailState()
        }
    }

    LaunchedEffect(state.game) {
        state.game?.let { game ->
            name = game.name
            type = game.type ?: ""
            minAge = game.minAge?.toString() ?: ""
            maxAge = game.maxAge?.toString() ?: ""
            selectedEditorId = game.editorId
        }
    }
    
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (gameId == null) "Nouveau Jeu" else "Détails Jeu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                actions = {
                    if (gameId != null && canManageGames) {
                        IconButton(onClick = { viewModel.deleteGame(gameId) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        if (state.isLoading || (gameId == null && editorState.isLoading)) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.errorMessage != null) {
                    Text(
                        "⚠️ ${state.errorMessage}",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (gameId == null) {
                    ExposedDropdownMenuBox(
                        expanded = expandedEditorDropdown,
                        onExpandedChange = { expandedEditorDropdown = !expandedEditorDropdown }
                    ) {
                        OutlinedTextField(
                            value = editorState.editors.find { it.id == selectedEditorId }?.name ?: "Sélectionner un éditeur",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Éditeur (Requis)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEditorDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedEditorDropdown,
                            onDismissRequest = { expandedEditorDropdown = false }
                        ) {
                            editorState.editors.forEach { editor ->
                                DropdownMenuItem(
                                    text = { Text(editor.name) },
                                    onClick = {
                                        selectedEditorId = editor.id
                                        expandedEditorDropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = state.game?.editorName ?: "Inconnu",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Éditeur") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom du jeu") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Type / Catégorie") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = minAge,
                        onValueChange = { minAge = it.filter { char -> char.isDigit() } },
                        label = { Text("Âge min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxAge,
                        onValueChange = { maxAge = it.filter { char -> char.isDigit() } },
                        label = { Text("Âge max") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = {
                        val parsedMin = minAge.toIntOrNull()
                        val parsedMax = maxAge.toIntOrNull()
                        val typeOpt = type.takeIf { it.isNotBlank() }

                        if (gameId == null) {
                            if (selectedEditorId != null) {
                                viewModel.createGame(
                                    GameCreateInput(
                                        editorId = selectedEditorId!!, 
                                        name = name, 
                                        type = typeOpt, 
                                        minAge = parsedMin, 
                                        maxAge = parsedMax
                                    )
                                )
                            }
                        } else {
                            viewModel.updateGame(
                                gameId, 
                                GameInput(
                                    name = name, 
                                    type = typeOpt, 
                                    minAge = parsedMin, 
                                    maxAge = parsedMax
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = canManageGames && !state.isSaving && name.isNotBlank() && (gameId != null || selectedEditorId != null)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(if (gameId == null) "Créer le jeu" else "Enregistrer")
                    }
                }
            }
        }
    }
}
