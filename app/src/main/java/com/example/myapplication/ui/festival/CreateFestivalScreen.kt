package com.example.myapplication.ui.festival

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.CreateFestivalRequest
import com.example.myapplication.model.ZoneTarifaireInput
import com.example.myapplication.ui.viewmodel.FestivalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFestivalScreen(
    viewModel: FestivalViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val state by viewModel.listState.collectAsState()

    // Champs principaux
    var nom by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var nombreTables by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Stock tables
    var stockStandard by remember { mutableStateOf("") }
    var stockGrandes by remember { mutableStateOf("") }
    var stockMairie by remember { mutableStateOf("") }
    var stockChaises by remember { mutableStateOf("") }

    // Dates via DatePicker
    var dateDebutMillis by remember { mutableStateOf<Long?>(null) }
    var dateFinMillis by remember { mutableStateOf<Long?>(null) }
    var showDateDebutPicker by remember { mutableStateOf(false) }
    var showDateFinPicker by remember { mutableStateOf(false) }
    val dateDebutPickerState = rememberDatePickerState()
    val dateFinPickerState = rememberDatePickerState()

    // Zones tarifaires
    var zones by remember { mutableStateOf(listOf<ZoneTarifaireInput>()) }
    var showZoneDialog by remember { mutableStateOf(false) }

    fun millisToDate(millis: Long): String {
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = millis
        return "%04d-%02d-%02d".format(
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    // DatePicker dialogs
    if (showDateDebutPicker) {
        DatePickerDialog(
            onDismissRequest = { showDateDebutPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateDebutMillis = dateDebutPickerState.selectedDateMillis
                    showDateDebutPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDateDebutPicker = false }) { Text("Annuler") }
            }
        ) { DatePicker(state = dateDebutPickerState) }
    }

    if (showDateFinPicker) {
        DatePickerDialog(
            onDismissRequest = { showDateFinPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateFinMillis = dateFinPickerState.selectedDateMillis
                    showDateFinPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDateFinPicker = false }) { Text("Annuler") }
            }
        ) { DatePicker(state = dateFinPickerState) }
    }

    // Calculs de validation (déclarés tôt pour être utilisés dans les dialogs)
    val totalTablesInt = nombreTables.toIntOrNull() ?: 0
    val stockSum = (stockStandard.toIntOrNull() ?: 0) +
                   (stockGrandes.toIntOrNull() ?: 0) +
                   (stockMairie.toIntOrNull() ?: 0)
    val stockExceeds = totalTablesInt > 0 && stockSum > totalTablesInt
    val zoneExceedsTotal = totalTablesInt > 0 && zones.any { it.nombreTables > totalTablesInt }
    val hasValidationError = stockExceeds || zoneExceedsTotal

    // Zone dialog
    if (showZoneDialog) {
        AddZoneDialog(
            maxTables = totalTablesInt,
            onDismiss = { showZoneDialog = false },
            onConfirm = { zone ->
                zones = zones + zone
                showZoneDialog = false
            }
        )
    }

    val dateDebutStr = dateDebutMillis?.let { millisToDate(it) } ?: ""
    val dateFinStr = dateFinMillis?.let { millisToDate(it) } ?: ""

    val canCreate = nom.isNotBlank() && location.isNotBlank() &&
        nombreTables.isNotBlank() && dateDebutStr.isNotBlank() &&
        dateFinStr.isNotBlank() && !state.isLoading && !hasValidationError

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouveau Festival", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
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
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        text = "⚠️ ${state.errorMessage}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // ── INFORMATIONS GÉNÉRALES ──────────────────────────────────
            SectionHeader("Informations générales")

            OutlinedTextField(
                value = nom,
                onValueChange = { nom = it },
                label = { Text("Nom du festival *") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Lieu / Ville *") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // ── DATES ────────────────────────────────────────────────────
            SectionHeader("Dates")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DatePickerField(
                    label = "Date début *",
                    value = dateDebutStr,
                    onClick = { showDateDebutPicker = true },
                    onClear = { dateDebutMillis = null },
                    modifier = Modifier.weight(1f)
                )
                DatePickerField(
                    label = "Date fin *",
                    value = dateFinStr,
                    onClick = { showDateFinPicker = true },
                    onClear = { dateFinMillis = null },
                    modifier = Modifier.weight(1f)
                )
            }

            // ── TABLES ───────────────────────────────────────────────────
            SectionHeader("Tables")

            OutlinedTextField(
                value = nombreTables,
                onValueChange = { nombreTables = it },
                label = { Text("Nombre total de tables *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            // Affichage récapitulatif du stock
            if (totalTablesInt > 0) {
                val stockColor = if (stockExceeds) MaterialTheme.colorScheme.error
                                 else MaterialTheme.colorScheme.onSurfaceVariant
                Text(
                    "Stock utilisé : $stockSum / $totalTablesInt tables (standard + grandes + mairie)",
                    fontSize = 12.sp,
                    color = stockColor
                )
                if (stockExceeds) {
                    Text(
                        "⚠ La somme des stocks (standard + grandes + mairie) dépasse le total de tables ($totalTablesInt).",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = stockStandard,
                    onValueChange = { stockStandard = it },
                    label = { Text("Standard") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = stockExceeds
                )
                OutlinedTextField(
                    value = stockGrandes,
                    onValueChange = { stockGrandes = it },
                    label = { Text("Grandes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = stockExceeds
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = stockMairie,
                    onValueChange = { stockMairie = it },
                    label = { Text("Mairie") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = stockExceeds
                )
                OutlinedTextField(
                    value = stockChaises,
                    onValueChange = { stockChaises = it },
                    label = { Text("Chaises (non comptées)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            // ── ZONES TARIFAIRES ─────────────────────────────────────────
            SectionHeader("Zones tarifaires")

            // Zones avec validation
            if (zoneExceedsTotal) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        "⚠ Une ou plusieurs zones dépassent le total de $totalTablesInt tables du festival.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp
                    )
                }
            }
            if (zones.isEmpty()) {
                Text(
                    "Aucune zone ajoutée. Vous pouvez en ajouter ci-dessous.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            } else {
                zones.forEachIndexed { index, zone ->
                    ZoneCard(
                        zone = zone,
                        totalTablesMax = totalTablesInt,
                        onDelete = { zones = zones.toMutableList().also { it.removeAt(index) } }
                    )
                }
            }

            OutlinedButton(
                onClick = { showZoneDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Ajouter une zone tarifaire")
            }

            // ── BOUTON CRÉER ──────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val request = CreateFestivalRequest(
                        nom = nom,
                        location = location,
                        nombreTotalTables = nombreTables.toIntOrNull() ?: 0,
                        dateDebut = dateDebutStr,
                        dateFin = dateFinStr,
                        description = description.takeIf { it.isNotBlank() },
                        stockTablesStandard = stockStandard.toIntOrNull() ?: 0,
                        stockTablesGrandes = stockGrandes.toIntOrNull() ?: 0,
                        stockTablesMairie = stockMairie.toIntOrNull() ?: 0,
                        stockChaises = stockChaises.toIntOrNull() ?: 0,
                        zones = zones
                    )
                    viewModel.createFestival(request, onSuccess = onSuccess)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = canCreate
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Créer le festival", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun DatePickerField(
    label: String,
    value: String,
    onClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    // Ne rien mettre ici, le bouton clear est externe
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = if (value.isNotEmpty()) MaterialTheme.colorScheme.primary
                                      else MaterialTheme.colorScheme.outline,
                disabledLabelColor = if (value.isNotEmpty()) MaterialTheme.colorScheme.primary
                                     else MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface
            )
        )
        Box(modifier = Modifier.matchParentSize().clickable { onClick() })
    }
}

@Composable
private fun ZoneCard(zone: ZoneTarifaireInput, totalTablesMax: Int, onDelete: () -> Unit) {
    val exceeds = totalTablesMax > 0 && zone.nombreTables > totalTablesMax
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (exceeds) MaterialTheme.colorScheme.errorContainer
                             else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(zone.nom, fontWeight = FontWeight.Bold)
                    Text(
                        "${zone.nombreTables} tables · ${zone.prixTable}€/table · ${zone.prixM2}€/m²",
                        fontSize = 13.sp,
                        color = if (exceeds) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                }
            }
            if (exceeds) {
                Text(
                    "⚠ ${zone.nombreTables} tables demandées mais le festival n'en a que $totalTablesMax au total.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun AddZoneDialog(
    maxTables: Int,
    onDismiss: () -> Unit,
    onConfirm: (ZoneTarifaireInput) -> Unit
) {
    var nom by remember { mutableStateOf("") }
    var nbTables by remember { mutableStateOf("") }
    var prixTable by remember { mutableStateOf("") }
    var prixM2 by remember { mutableStateOf("") }

    val nbTablesInt = nbTables.toIntOrNull() ?: 0
    val tablesExceeds = maxTables > 0 && nbTablesInt > maxTables

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle zone tarifaire") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom de la zone *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nbTables,
                    onValueChange = { nbTables = it },
                    label = {
                        Text(if (maxTables > 0) "Nombre de tables * (max $maxTables)" else "Nombre de tables *")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = tablesExceeds,
                    modifier = Modifier.fillMaxWidth()
                )
                if (tablesExceeds) {
                    Text(
                        "⚠ Maximum $maxTables tables pour ce festival",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = prixTable,
                        onValueChange = { prixTable = it },
                        label = { Text("Prix/table (€)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = prixM2,
                        onValueChange = { prixM2 = it },
                        label = { Text("Prix/m² (€)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        ZoneTarifaireInput(
                            nom = nom.trim(),
                            nombreTables = nbTablesInt,
                            prixTable = prixTable.toDoubleOrNull() ?: 0.0,
                            prixM2 = prixM2.toDoubleOrNull() ?: 0.0
                        )
                    )
                },
                enabled = nom.isNotBlank() && nbTables.isNotBlank() && !tablesExceeds
            ) { Text("Ajouter") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

