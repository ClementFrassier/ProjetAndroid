package com.example.myapplication.ui.festival

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.CreateFestivalRequest
import com.example.myapplication.ui.viewmodel.FestivalViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFestivalScreen(
    viewModel: FestivalViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var nom by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var nombreTables by remember { mutableStateOf("") }
    var dateDebut by remember { mutableStateOf("") }
    var dateFin by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val state by viewModel.listState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouveau Festival") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            OutlinedTextField(
                value = nom,
                onValueChange = { nom = it },
                label = { Text("Nom du festival *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Lieu *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = nombreTables,
                onValueChange = { nombreTables = it },
                label = { Text("Nombre total de tables *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = dateDebut,
                onValueChange = { dateDebut = it },
                label = { Text("Date début (YYYY-MM-DD) *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = dateFin,
                onValueChange = { dateFin = it },
                label = { Text("Date fin (YYYY-MM-DD) *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = {
                    val tables = nombreTables.toIntOrNull() ?: 0
                    val request = CreateFestivalRequest(
                        nom = nom,
                        location = location,
                        nombreTotalTables = tables,
                        dateDebut = dateDebut,
                        dateFin = dateFin,
                        description = description.takeIf { it.isNotBlank() }
                    )
                    viewModel.createFestival(request, onSuccess = onSuccess)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = nom.isNotBlank() && location.isNotBlank() && nombreTables.isNotBlank() && dateDebut.isNotBlank() && dateFin.isNotBlank() && !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Créer le festival")
                }
            }
        }
    }
}
