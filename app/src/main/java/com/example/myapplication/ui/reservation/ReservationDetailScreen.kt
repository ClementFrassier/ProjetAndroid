package com.example.myapplication.ui.reservation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.myapplication.model.ReservationCreateInput
import com.example.myapplication.model.ReservationLineInput
import com.example.myapplication.model.ReservationUpdateInput
import com.example.myapplication.ui.viewmodel.AuthViewModel
import com.example.myapplication.ui.viewmodel.EditorViewModel
import com.example.myapplication.ui.viewmodel.FestivalViewModel
import com.example.myapplication.ui.viewmodel.ReservationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailScreen(
    festivalId: Int,
    reservationId: Int?,
    viewModel: ReservationViewModel,
    authViewModel: AuthViewModel,
    festivalViewModel: FestivalViewModel,
    editorViewModel: EditorViewModel,
    onManageInvoice: (Int) -> Unit,
    onManagePlacement: (Int) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.detailState.collectAsState()
    val festivalState by festivalViewModel.detailState.collectAsState()
    val editorState by editorViewModel.listState.collectAsState()
    val canManageReservations = authViewModel.canManageReservations()

    var selectedEditorId by remember { mutableStateOf<Int?>(null) }
    var expandedEditorDropdown by remember { mutableStateOf(false) }

    var willPresentGames by remember { mutableStateOf(true) }
    var powerOutlets by remember { mutableStateOf("0") }
    var gamesNotes by remember { mutableStateOf("") }
    var localValidationMessage by remember { mutableStateOf<String?>(null) }

    // Map: TariffZoneId -> Tables count
    val zoneTablesMap = remember { mutableStateMapOf<Int, String>() }

    LaunchedEffect(Unit) {
        festivalViewModel.loadFestival(festivalId)
        if (reservationId == null) {
            editorViewModel.loadEditors()
        }
    }

    LaunchedEffect(reservationId) {
        if (reservationId != null) {
            viewModel.loadReservation(reservationId)
        } else {
            viewModel.resetDetailState()
        }
    }

    LaunchedEffect(state.reservation) {
        state.reservation?.let { res ->
            if (reservationId != null) {
                // Initialiser le formulaire pour édition
                zoneTablesMap.clear()
                selectedEditorId = res.editorId
                willPresentGames = res.willPresentGames
                powerOutlets = res.powerOutlets.toString()
                gamesNotes = res.gamesNotes ?: ""
                res.lines?.forEach { line ->
                    zoneTablesMap[line.tariffZoneId] = line.tablesCount.toString()
                }
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
                title = { Text(if (reservationId == null) "Nouvelle Réservation" else "Détails Réservation", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                actions = {
                    if (reservationId != null && canManageReservations) {
                        IconButton(onClick = { viewModel.deleteReservation(reservationId, festivalId) }) {
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
        if (state.isLoading || festivalState.isLoading || (reservationId == null && editorState.isLoading)) {
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
                        Text("⚠️ ${state.errorMessage}", color = MaterialTheme.colorScheme.error)
                    }
                }

                if (localValidationMessage != null) {
                    item {
                        Text("⚠️ $localValidationMessage", color = MaterialTheme.colorScheme.error)
                    }
                }

                item {
                    Text("Éditeur & Options", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                }

                item {
                    if (reservationId == null) {
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
                            value = state.reservation?.editorName ?: "Inconnu",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Éditeur") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = willPresentGames,
                            onCheckedChange = { willPresentGames = it }
                        )
                        Text("Présentera des jeux")
                    }
                }

                item {
                    OutlinedTextField(
                        value = powerOutlets,
                        onValueChange = { powerOutlets = it.filter { char -> char.isDigit() } },
                        label = { Text("Prises électriques requises") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = gamesNotes,
                        onValueChange = { gamesNotes = it },
                        label = { Text("Notes spécifiques (jeux)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                item { Divider(Modifier.padding(vertical = 8.dp)) }

                item {
                    Text("Zones Tarifaires", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Sélectionnez le nombre de tables pour chaque zone", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                val zones = festivalState.festival?.tariffZones ?: emptyList()
                if (zones.isEmpty()) {
                    item { Text("Aucune zone tarifaire définie pour ce festival") }
                } else {
                    items(zones) { zone ->
                        val currentTables = zoneTablesMap[zone.id]?.toIntOrNull() ?: 0
                        val exceeds = currentTables > zone.totalTables
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(zone.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${zone.pricePerTable}€/table · max ${zone.totalTables} tables",
                                    fontSize = 12.sp,
                                    color = if (exceeds) MaterialTheme.colorScheme.error
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedTextField(
                                value = zoneTablesMap[zone.id] ?: "0",
                                onValueChange = { v ->
                                    val n = v.filter { it.isDigit() }.take(3)
                                    zoneTablesMap[zone.id] = n
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(80.dp),
                                singleLine = true,
                                isError = exceeds,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (exceeds) MaterialTheme.colorScheme.error
                                                         else MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        if (exceeds) {
                            Text(
                                "⚠ Maximum ${zone.totalTables} tables pour cette zone",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    Button(
                        onClick = {
                            localValidationMessage = null
                            val outlets = powerOutlets.toIntOrNull() ?: 0
                            val zones = festivalState.festival?.tariffZones ?: emptyList()

                            // Vérification : aucune zone ne dépasse son nombre max de tables
                            val overflowZone = zones.firstOrNull { zone ->
                                val requested = zoneTablesMap[zone.id]?.toIntOrNull() ?: 0
                                requested > zone.totalTables
                            }
                            if (overflowZone != null) {
                                val requested = zoneTablesMap[overflowZone.id]?.toIntOrNull() ?: 0
                                localValidationMessage = "Zone \"${overflowZone.name}\" : $requested tables demandées mais maximum ${overflowZone.totalTables} disponibles."
                                return@Button
                            }

                            val lines = zoneTablesMap.mapNotNull { (zoneId, tablesStr) ->
                                val count = tablesStr.toIntOrNull() ?: 0
                                if (count > 0) ReservationLineInput(tariffZoneId = zoneId, tablesCount = count) else null
                            }

                            if (lines.isEmpty()) {
                                localValidationMessage = "Sélectionne au moins une zone avec un nombre de tables."
                                return@Button
                            }

                            if (reservationId == null) {
                                if (selectedEditorId != null) {
                                    viewModel.createReservation(
                                        ReservationCreateInput(
                                            festivalId = festivalId,
                                            editorId = selectedEditorId,
                                            willPresentGames = willPresentGames,
                                            powerOutlets = outlets,
                                            gamesNotes = gamesNotes.takeIf { it.isNotBlank() },
                                            lines = lines
                                        ),
                                        festivalId
                                    )
                                }
                            } else {
                                viewModel.updateReservation(
                                    reservationId,
                                    ReservationUpdateInput(
                                        willPresentGames = willPresentGames,
                                        powerOutlets = outlets,
                                        gamesNotes = gamesNotes.takeIf { it.isNotBlank() },
                                        lines = lines
                                    ),
                                    festivalId
                                )
                            }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = canManageReservations && !state.isSaving &&
                        (selectedEditorId != null || reservationId != null) &&
                        (festivalState.festival?.tariffZones?.none { zone ->
                            (zoneTablesMap[zone.id]?.toIntOrNull() ?: 0) > zone.totalTables
                        } != false)
                ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(if (reservationId == null) "Créer la réservation" else "Mettre à jour")
                        }
                    }
                }

                if (reservationId != null && canManageReservations) {
                    item {
                        OutlinedButton(
                            onClick = { onManageInvoice(reservationId) },
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Gérer la facture")
                        }
                    }
                }

                if (reservationId != null && authViewModel.canManagePlacement()) {
                    item {
                        OutlinedButton(
                            onClick = { onManagePlacement(reservationId) },
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Jeux & placement")
                        }
                    }
                }
            }
        }
    }
}
