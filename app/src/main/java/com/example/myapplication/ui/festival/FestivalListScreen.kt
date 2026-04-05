package com.example.myapplication.ui.festival

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.Festival
import com.example.myapplication.ui.viewmodel.AuthViewModel
import com.example.myapplication.ui.viewmodel.FestivalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FestivalListScreen(
    festivalViewModel: FestivalViewModel,
    authViewModel: AuthViewModel,
    onFestivalClick: (Int) -> Unit,
    onLogout: () -> Unit,
    onLogin: () -> Unit,
    onNavigateToEditors: () -> Unit = {},
    onNavigateToGames: () -> Unit = {},
    onNavigateToCreateFestival: () -> Unit = {}
) {
    val state by festivalViewModel.listState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val canManageFestivals = authViewModel.canManageFestivals()

    var searchName by remember { mutableStateOf("") }
    var searchCity by remember { mutableStateOf("") }
    var filterDate by remember { mutableStateOf<String?>(null) } // format "YYYY-MM-DD"
    var sortOption by remember { mutableStateOf("date") } // "date", "nom", "ville"
    var showFilters by remember { mutableStateOf(false) }
    var expandedSort by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    LaunchedEffect(Unit) {
        festivalViewModel.loadFestivals()
    }

    val filteredFestivals = remember(state.festivals, searchName, searchCity, filterDate, sortOption) {
        state.festivals
            .filter { festival ->
                val matchName = searchName.isBlank() || festival.name.contains(searchName, ignoreCase = true)
                val matchCity = searchCity.isBlank() || festival.location.contains(searchCity, ignoreCase = true)
                val matchDate = filterDate == null ||
                    (festival.dateDebut.take(10) <= filterDate!! && festival.dateFin.take(10) >= filterDate!!)
                matchName && matchCity && matchDate
            }
            .sortedWith(when (sortOption) {
                "nom" -> compareBy { it.name.lowercase() }
                "ville" -> compareBy { it.location.lowercase() }
                else -> compareByDescending { it.dateDebut }
            })
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                        cal.timeInMillis = millis
                        val y = cal.get(java.util.Calendar.YEAR)
                        val m = cal.get(java.util.Calendar.MONTH) + 1
                        val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
                        filterDate = "%04d-%02d-%02d".format(y, m, d)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Festivals AWI", fontWeight = FontWeight.Bold)
                        if (authState.userLogin != null) {
                            Text(
                                "${authState.userLogin} · ${authState.userRole}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (authState.isLoggedIn) {
                        IconButton(onClick = onNavigateToGames) {
                            Icon(Icons.Filled.Casino, contentDescription = "Jeux")
                        }
                        IconButton(onClick = onNavigateToEditors) {
                            Icon(Icons.Filled.Business, contentDescription = "Éditeurs")
                        }
                        IconButton(onClick = {
                            authViewModel.logout()
                            onLogout()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Déconnexion")
                        }
                    } else {
                        TextButton(onClick = onLogin) {
                            Text("Se connecter")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            if (authState.isLoggedIn && canManageFestivals) {
                FloatingActionButton(onClick = onNavigateToCreateFestival) {
                    Icon(Icons.Default.Add, contentDescription = "Créer un festival")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Barre de recherche par NOM
            OutlinedTextField(
                value = searchName,
                onValueChange = { searchName = it },
                placeholder = { Text("Rechercher par nom de festival...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Rechercher") },
                trailingIcon = {
                    Row {
                        if (searchName.isNotEmpty()) {
                            IconButton(onClick = { searchName = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Effacer")
                            }
                        }
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(
                                Icons.Filled.FilterList,
                                contentDescription = "Trier/Filtrer",
                                tint = if (showFilters || searchCity.isNotEmpty()) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                singleLine = true
            )

            // Filtres et tri (panneau dépliable)
            if (showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Filtre par VILLE
                        OutlinedTextField(
                            value = searchCity,
                            onValueChange = { searchCity = it },
                            label = { Text("Filtrer par ville") },
                            leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                            trailingIcon = {
                                if (searchCity.isNotEmpty()) {
                                    IconButton(onClick = { searchCity = "" }) {
                                        Icon(Icons.Filled.Clear, contentDescription = "Effacer")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Filtre par DATE
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = filterDate ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    label = { Text("Actifs à cette date") },
                                    placeholder = { Text("Sélectionner...") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.CalendarMonth,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = if (filterDate != null)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outline,
                                        disabledLabelColor = if (filterDate != null)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                // Overlay transparent pour ouvrir le picker
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { showDatePicker = true }
                                )
                            }
                            // Bouton croix HORS de l'overlay
                            if (filterDate != null) {
                                IconButton(onClick = { filterDate = null }) {
                                    Icon(
                                        Icons.Filled.Clear,
                                        contentDescription = "Effacer la date",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        // Option de TRI
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Trier par :", fontWeight = FontWeight.Medium)
                            ExposedDropdownMenuBox(
                                expanded = expandedSort,
                                onExpandedChange = { expandedSort = !expandedSort },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = when (sortOption) {
                                        "nom" -> "Nom"
                                        "ville" -> "Ville"
                                        else -> "Date"
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSort) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedSort,
                                    onDismissRequest = { expandedSort = false }
                                ) {
                                    listOf("date" to "Date", "nom" to "Nom", "ville" to "Ville").forEach { (key, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                sortOption = key
                                                expandedSort = false
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
                    state.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    state.errorMessage != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⚠️ ${state.errorMessage}", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { festivalViewModel.loadFestivals() }) {
                                Text("Réessayer")
                            }
                        }
                    }
                    filteredFestivals.isEmpty() -> {
                        val hasFilter = searchName.isNotBlank() || searchCity.isNotBlank()
                        Text(
                            if (!hasFilter) "Aucun festival trouvé"
                            else "Aucun festival correspondant à ces critères",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredFestivals, key = { it.id }) { festival ->
                                FestivalCard(
                                    festival = festival,
                                    onClick = { onFestivalClick(festival.id) }
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
private fun FestivalCard(festival: Festival, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = festival.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(festival.location, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${festival.dateDebut.take(10)} → ${festival.dateFin.take(10)}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!festival.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    festival.description,
                    fontSize = 13.sp,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("${festival.totalTables} tables") }
                )
                if (festival.tariffZones.isNotEmpty()) {
                    AssistChip(
                        onClick = {},
                        label = { Text("${festival.tariffZones.size} zones") }
                    )
                }
            }
        }
    }
}
