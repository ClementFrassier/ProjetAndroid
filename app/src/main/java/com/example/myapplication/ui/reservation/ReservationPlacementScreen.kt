package com.example.myapplication.ui.reservation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.Game
import com.example.myapplication.model.Reservation
import com.example.myapplication.model.ReservationGamePlacement
import com.example.myapplication.model.ReservationGamePlacementCreateInput
import com.example.myapplication.model.ReservationGamePlacementUpdateInput
import com.example.myapplication.model.ZonePlan
import com.example.myapplication.model.ZonePlanInput
import com.example.myapplication.ui.viewmodel.AuthViewModel
import com.example.myapplication.ui.viewmodel.EditorViewModel
import com.example.myapplication.ui.viewmodel.FestivalViewModel
import com.example.myapplication.ui.viewmodel.ReservationPlacementViewModel
import com.example.myapplication.ui.viewmodel.ReservationViewModel
import com.example.myapplication.ui.viewmodel.ZonePlanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationPlacementScreen(
    festivalId: Int,
    reservationId: Int,
    reservationViewModel: ReservationViewModel,
    festivalViewModel: FestivalViewModel,
    editorViewModel: EditorViewModel,
    authViewModel: AuthViewModel,
    zonePlanViewModel: ZonePlanViewModel,
    placementViewModel: ReservationPlacementViewModel,
    onBack: () -> Unit
) {
    val reservationState by reservationViewModel.detailState.collectAsState()
    val festivalState by festivalViewModel.detailState.collectAsState()
    val editorGamesState by editorViewModel.gamesState.collectAsState()
    val zonePlanState by zonePlanViewModel.uiState.collectAsState()
    val placementState by placementViewModel.uiState.collectAsState()
    val canManagePlacement = authViewModel.canManagePlacement()

    var zoneName by remember { mutableStateOf("") }
    var selectedTariffZoneId by remember { mutableStateOf<Int?>(null) }
    var zoneTablesCount by remember { mutableStateOf("1") }
    var selectedGameId by remember { mutableStateOf<Int?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var usedTables by remember { mutableStateOf("1") }
    var selectedZonePlanId by remember { mutableStateOf<Int?>(null) }
    var tableType by remember { mutableStateOf("standard") }
    var requestedList by remember { mutableStateOf(false) }
    var obtainedList by remember { mutableStateOf(false) }
    var receivedGames by remember { mutableStateOf(false) }
    var localMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(festivalId, reservationId) {
        reservationViewModel.loadReservation(reservationId)
        festivalViewModel.loadFestival(festivalId)
        zonePlanViewModel.loadByFestival(festivalId)
        placementViewModel.loadByReservation(reservationId)
    }

    LaunchedEffect(reservationState.reservation?.editorId) {
        reservationState.reservation?.editorId?.let { editorViewModel.loadEditorGames(it) }
    }

    val reservation = reservationState.reservation
    val festival = festivalState.festival
    val availableGames = editorGamesState.games
    val eligibleZones = remember(reservation, zonePlanState.zones) {
        computeEligibleZones(reservation, zonePlanState.zones)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jeux & placement", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        if (reservationState.isLoading || festivalState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
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
                if (!canManagePlacement) {
                    item {
                        Text(
                            "Accès réservé aux organisateurs pour le placement des jeux.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                listOfNotNull(
                    reservationState.errorMessage,
                    editorGamesState.errorMessage,
                    zonePlanState.errorMessage,
                    placementState.errorMessage,
                    localMessage
                ).forEach { message ->
                    item {
                        Text("⚠️ $message", color = MaterialTheme.colorScheme.error)
                    }
                }

                if (reservation != null) {
                    item {
                        ReservationPlacementSummaryCard(
                            reservation = reservation,
                            availableGames = availableGames.size,
                            placedGames = placementState.placements.size
                        )
                    }
                }

                item {
                    SectionCard(title = "Zones du plan") {
                        Text(
                            "Les zones du plan doivent rester compatibles avec les zones tarifaires du festival.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (canManagePlacement && festival != null) {
                            OutlinedTextField(
                                value = zoneName,
                                onValueChange = { zoneName = it },
                                label = { Text("Nom de la zone") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            IntSelectionField(
                                label = "Zone tarifaire",
                                value = festival.tariffZones.find { it.id == selectedTariffZoneId }?.name ?: "Choisir une zone",
                                options = festival.tariffZones.map { it.id to it.name },
                                onSelect = { selectedTariffZoneId = it },
                                enabled = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = zoneTablesCount,
                                onValueChange = { zoneTablesCount = it.filter(Char::isDigit) },
                                label = { Text("Nombre de tables") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    localMessage = null
                                    val tariffZoneId = selectedTariffZoneId
                                    val tables = zoneTablesCount.toIntOrNull() ?: 0
                                    if (tariffZoneId == null || zoneName.isBlank() || tables <= 0) {
                                        localMessage = "Complète le nom, la zone tarifaire et le nombre de tables."
                                    } else {
                                        zonePlanViewModel.create(
                                            ZonePlanInput(
                                                festivalId = festivalId,
                                                tariffZoneId = tariffZoneId,
                                                name = zoneName.trim(),
                                                tablesCount = tables
                                            ),
                                            festivalId
                                        )
                                        zoneName = ""
                                        selectedTariffZoneId = null
                                        zoneTablesCount = "1"
                                    }
                                },
                                enabled = !zonePlanState.isLoading
                            ) {
                                Text("Ajouter une zone du plan")
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (zonePlanState.zones.isEmpty()) {
                            Text("Aucune zone du plan.")
                        } else {
                            zonePlanState.zones.forEach { zone ->
                                ZonePlanCard(
                                    zone = zone,
                                    tariffZoneName = festival?.tariffZones?.find { it.id == zone.tariffZoneId }?.name,
                                    canManagePlacement = canManagePlacement,
                                    onDelete = { zonePlanViewModel.delete(festivalId, zone.id) }
                                )
                            }
                        }
                    }
                }

                item {
                    SectionCard(title = "Placement des jeux") {
                        if (reservation == null) {
                            Text("Réservation introuvable.")
                        } else {
                            if (editorGamesState.isLoading) {
                                Text("Chargement des jeux de l'éditeur...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                            } else if (availableGames.isEmpty()) {
                                Text(
                                    "Aucun jeu disponible pour l'éditeur de cette réservation.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (canManagePlacement) {
                                IntSelectionField(
                                    label = "Jeu",
                                    value = availableGames.find { it.id == selectedGameId }?.name ?: "Choisir un jeu",
                                    options = availableGames.map { it.id to it.name },
                                    onSelect = { selectedGameId = it },
                                    enabled = availableGames.isNotEmpty()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                IntSelectionField(
                                    label = "Zone du plan",
                                    value = eligibleZones.find { it.id == selectedZonePlanId }?.name ?: "Sans zone",
                                    options = eligibleZones.map { it.id to "${it.name} (${it.tablesCount} tables)" },
                                    onSelect = { selectedZonePlanId = it },
                                    allowClear = true,
                                    enabled = eligibleZones.isNotEmpty()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = quantity,
                                        onValueChange = { quantity = it.filter(Char::isDigit) },
                                        label = { Text("Quantité") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = usedTables,
                                        onValueChange = { usedTables = it.filter(Char::isDigit) },
                                        label = { Text("Tables utilisées") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                StringSelectionField(
                                    label = "Type de table",
                                    value = tableType,
                                    options = listOf(
                                        "standard" to "standard",
                                        "grande" to "grande",
                                        "mairie" to "mairie"
                                    ),
                                    onSelect = { tableType = it },
                                    enabled = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                PlacementFlags(
                                    requestedList = requestedList,
                                    obtainedList = obtainedList,
                                    receivedGames = receivedGames,
                                    onRequestedListChange = { requestedList = it },
                                    onObtainedListChange = { obtainedList = it },
                                    onReceivedGamesChange = { receivedGames = it }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        localMessage = null
                                        val gameId = selectedGameId
                                        val qty = quantity.toIntOrNull() ?: 0
                                        val tables = usedTables.toIntOrNull() ?: 0
                                        if (gameId == null || qty <= 0 || tables <= 0) {
                                            localMessage = "Sélectionne un jeu et renseigne une quantité et des tables utilisées valides."
                                        } else {
                                            placementViewModel.create(
                                                ReservationGamePlacementCreateInput(
                                                    gameId = gameId,
                                                    reservationId = reservationId,
                                                    zonePlanId = selectedZonePlanId,
                                                    quantity = qty,
                                                    allocatedTables = tables,
                                                    tableType = tableType,
                                                    usedTables = tables,
                                                    requestedList = requestedList,
                                                    obtainedList = obtainedList,
                                                    receivedGames = receivedGames
                                                )
                                            )
                                            selectedGameId = null
                                            selectedZonePlanId = null
                                            quantity = "1"
                                            usedTables = "1"
                                            tableType = "standard"
                                            requestedList = false
                                            obtainedList = false
                                            receivedGames = false
                                        }
                                    },
                                    enabled = !placementState.isLoading && availableGames.isNotEmpty()
                                ) {
                                    Text("Ajouter un jeu")
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            if (placementState.placements.isEmpty()) {
                                Text("Aucun jeu placé pour cette réservation.")
                            } else {
                                placementState.placements.forEach { placement ->
                                    PlacementCard(
                                        placement = placement,
                                        eligibleZones = eligibleZones,
                                        canManagePlacement = canManagePlacement,
                                        onSave = { updated ->
                                            placementViewModel.update(placement.id, updated, reservationId)
                                        },
                                        onDelete = {
                                            placementViewModel.delete(placement.id, reservationId)
                                        }
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
private fun ReservationPlacementSummaryCard(
    reservation: Reservation,
    availableGames: Int,
    placedGames: Int
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(reservation.editorName ?: "Réservation #${reservation.id}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Statut : ${reservation.workflowState}")
            Text("Tables réservées : ${reservation.totalTables}")
            Text("Jeux disponibles chez l'éditeur : $availableGames")
            Text("Jeux déjà placés : $placedGames")
        }
    }
}

@Composable
private fun ZonePlanCard(
    zone: ZonePlan,
    tariffZoneName: String?,
    canManagePlacement: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(zone.name, fontWeight = FontWeight.Bold)
                Text("Zone tarifaire : ${tariffZoneName ?: "#${zone.tariffZoneId}"}", fontSize = 13.sp)
                Text("${zone.tablesCount} tables", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (canManagePlacement) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Supprimer la zone")
                }
            }
        }
    }
}

@Composable
private fun PlacementCard(
    placement: ReservationGamePlacement,
    eligibleZones: List<ZonePlan>,
    canManagePlacement: Boolean,
    onSave: (ReservationGamePlacementUpdateInput) -> Unit,
    onDelete: () -> Unit
) {
    var localQuantity by remember(placement.id) { mutableStateOf(placement.quantity.toString()) }
    var localUsedTables by remember(placement.id) { mutableStateOf(placement.usedTables.toString()) }
    var localZonePlanId by remember(placement.id) { mutableStateOf(placement.zonePlanId) }
    var localTableType by remember(placement.id) { mutableStateOf(placement.tableType) }
    var localRequestedList by remember(placement.id) { mutableStateOf(placement.requestedList == true) }
    var localObtainedList by remember(placement.id) { mutableStateOf(placement.obtainedList == true) }
    var localReceivedGames by remember(placement.id) { mutableStateOf(placement.receivedGames == true) }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(placement.gameName ?: "Jeu #${placement.gameId}", fontWeight = FontWeight.Bold)
            Text("Zone actuelle : ${placement.zoneName ?: "Non placée"}", fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = localQuantity,
                    onValueChange = { localQuantity = it.filter(Char::isDigit) },
                    label = { Text("Quantité") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    enabled = canManagePlacement
                )
                OutlinedTextField(
                    value = localUsedTables,
                    onValueChange = { localUsedTables = it.filter(Char::isDigit) },
                    label = { Text("Tables") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    enabled = canManagePlacement
                )
            }
            IntSelectionField(
                label = "Zone du plan",
                value = eligibleZones.find { it.id == localZonePlanId }?.name ?: "Sans zone",
                options = eligibleZones.map { it.id to "${it.name} (${it.tablesCount} tables)" },
                onSelect = { localZonePlanId = it },
                allowClear = true,
                enabled = canManagePlacement
            )
            StringSelectionField(
                label = "Type de table",
                value = localTableType,
                options = listOf(
                    "standard" to "standard",
                    "grande" to "grande",
                    "mairie" to "mairie"
                ),
                onSelect = { localTableType = it },
                enabled = canManagePlacement
            )
            PlacementFlags(
                requestedList = localRequestedList,
                obtainedList = localObtainedList,
                receivedGames = localReceivedGames,
                onRequestedListChange = { localRequestedList = it },
                onObtainedListChange = { localObtainedList = it },
                onReceivedGamesChange = { localReceivedGames = it },
                enabled = canManagePlacement
            )
            if (canManagePlacement) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            onSave(
                                ReservationGamePlacementUpdateInput(
                                    zonePlanId = localZonePlanId,
                                    quantity = localQuantity.toIntOrNull(),
                                    allocatedTables = localUsedTables.toIntOrNull(),
                                    tableType = localTableType,
                                    usedTables = localUsedTables.toIntOrNull(),
                                    requestedList = localRequestedList,
                                    obtainedList = localObtainedList,
                                    receivedGames = localReceivedGames
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Enregistrer")
                    }
                    OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                        Text("Supprimer")
                    }
                }
            }
        }
    }
}

@Composable
private fun PlacementFlags(
    requestedList: Boolean,
    obtainedList: Boolean,
    receivedGames: Boolean,
    onRequestedListChange: (Boolean) -> Unit,
    onObtainedListChange: (Boolean) -> Unit,
    onReceivedGamesChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        FlagRow("Liste demandée", requestedList, onRequestedListChange, enabled)
        FlagRow("Liste obtenue", obtainedList, onObtainedListChange, enabled)
        FlagRow("Jeux reçus", receivedGames, onReceivedGamesChange, enabled)
    }
}

@Composable
private fun FlagRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        Text(label)
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
            content()
        }
    }
}

@Composable
private fun IntSelectionField(
    label: String,
    value: String,
    options: List<Pair<Int, String>>,
    onSelect: (Int?) -> Unit,
    allowClear: Boolean = false,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(
            onClick = { if (enabled) expanded = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        ) {
            Text(value)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (allowClear) {
                DropdownMenuItem(
                    text = { Text("Sans zone") },
                    onClick = {
                        onSelect(null)
                        expanded = false
                    }
                )
            }
            options.forEach { (id, labelValue) ->
                DropdownMenuItem(
                    text = { Text(labelValue) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun StringSelectionField(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(
            onClick = { if (enabled) expanded = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        ) {
            Text(value)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, labelValue) ->
                DropdownMenuItem(
                    text = { Text(labelValue) },
                    onClick = {
                        onSelect(key)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun computeEligibleZones(reservation: Reservation?, zones: List<ZonePlan>): List<ZonePlan> {
    val allowedZoneIds = reservation?.lines
        ?.map { it.tariffZoneId }
        ?.toSet()
        .orEmpty()

    if (allowedZoneIds.isEmpty()) return zones
    return zones.filter { it.tariffZoneId in allowedZoneIds }
}
