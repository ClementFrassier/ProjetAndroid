package com.example.myapplication.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.ContactInput
import com.example.myapplication.model.EditorInput
import com.example.myapplication.model.GameInput
import com.example.myapplication.ui.viewmodel.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorDetailScreen(
    editorId: Int?, 
    viewModel: EditorViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.detailState.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var contacts by remember { mutableStateOf(listOf<ContactInput>()) }
    var games by remember { mutableStateOf(listOf<GameInput>()) }

    LaunchedEffect(editorId) {
        if (editorId != null) {
            viewModel.loadEditor(editorId)
        } else {
            viewModel.resetDetailState()
        }
    }

    LaunchedEffect(state.editor) {
        state.editor?.let { editor ->
            name = editor.name
            contacts = editor.contacts.map { 
                ContactInput(id = it.id, fullName = it.fullName, email = it.email, phone = it.phone, isPrimary = it.isPrimary) 
            }
            games = editor.games.map { 
                GameInput(id = it.id, name = it.name, type = it.type, minAge = it.minAge, maxAge = it.maxAge) 
            }
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
                title = { Text(if (editorId == null) "Nouvel Éditeur" else "Détails Éditeur", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                actions = {
                    if (editorId != null) {
                        IconButton(onClick = { viewModel.deleteEditor(editorId) }) {
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
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.errorMessage != null) {
                    item {
                        Text(
                            "⚠️ ${state.errorMessage}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom de l'éditeur") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Contacts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(contacts) { contact ->
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            OutlinedTextField(
                                value = contact.fullName,
                                onValueChange = { newName ->
                                    contacts = contacts.map { if (it === contact) it.copy(fullName = newName) else it }
                                },
                                label = { Text("Nom Complet") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = contact.email ?: "",
                                onValueChange = { newEmail ->
                                    contacts = contacts.map { if (it === contact) it.copy(email = newEmail) else it }
                                },
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = contact.isPrimary ?: false,
                                    onCheckedChange = { isChecked ->
                                        contacts = contacts.map { if (it === contact) it.copy(isPrimary = isChecked) else it }
                                    }
                                )
                                Text("Contact Principal")
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { contacts = contacts.filter { it !== contact } }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                                }
                            }
                        }
                    }
                }

                item {
                    TextButton(onClick = { contacts = contacts + ContactInput(fullName = "") }) {
                        Text("+ Ajouter un contact")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Jeux", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                items(games) { game ->
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            OutlinedTextField(
                                value = game.name,
                                onValueChange = { newName ->
                                    games = games.map { if (it === game) it.copy(name = newName) else it }
                                },
                                label = { Text("Nom du jeu") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = game.type ?: "",
                                onValueChange = { newType ->
                                    games = games.map { if (it === game) it.copy(type = newType) else it }
                                },
                                label = { Text("Type / Catégorie") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            IconButton(onClick = { games = games.filter { it !== game } }, modifier = Modifier.align(Alignment.End)) {
                                Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                            }
                        }
                    }
                }

                item {
                    TextButton(onClick = { games = games + GameInput(name = "") }) {
                        Text("+ Ajouter un jeu")
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = {
                            val input = EditorInput(name, contacts = contacts, games = games)
                            if (editorId == null) {
                                viewModel.createEditor(input)
                            } else {
                                viewModel.updateEditor(editorId, input)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !state.isSaving && name.isNotBlank()
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(if (editorId == null) "Créer" else "Enregistrer")
                        }
                    }
                }
            }
        }
    }
}
