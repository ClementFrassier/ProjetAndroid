package com.example.myapplication.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.EditorInput
import com.example.myapplication.ui.viewmodel.AuthViewModel
import com.example.myapplication.ui.viewmodel.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorDetailScreen(
    editorId: Int?,
    viewModel: EditorViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.detailState.collectAsState()
    val canManageEditors = authViewModel.canManageFestivals()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var typeReservant by remember { mutableStateOf("editeur") }
    var estReservant by remember { mutableStateOf(true) }

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
            description = editor.description.orEmpty()
            typeReservant = editor.typeReservant ?: "editeur"
            estReservant = editor.estReservant ?: true
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
                    if (editorId != null && canManageEditors) {
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
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

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = typeReservant,
                    onValueChange = { typeReservant = it },
                    label = { Text("Type réservant") },
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = estReservant,
                            onCheckedChange = { estReservant = it }
                        )
                        Text("Peut réserver un stand")
                    }
                }

                if (state.editor?.games?.isNotEmpty() == true) {
                    Text("Jeux liés", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    state.editor!!.games.forEach { game ->
                        Text("• ${game.name}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val input = EditorInput(
                            name = name,
                            description = description.takeIf { it.isNotBlank() },
                            typeReservant = typeReservant.takeIf { it.isNotBlank() },
                            estReservant = estReservant
                        )
                        if (editorId == null) {
                            viewModel.createEditor(input)
                        } else {
                            viewModel.updateEditor(editorId, input)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = canManageEditors && !state.isSaving && name.isNotBlank()
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (editorId == null) "Créer" else "Enregistrer")
                    }
                }
            }
        }
    }
}
