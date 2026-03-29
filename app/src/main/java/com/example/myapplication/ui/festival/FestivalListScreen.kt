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

    LaunchedEffect(Unit) {
        festivalViewModel.loadFestivals()
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
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
                        Button(onClick = { festivalViewModel.loadFestivals() }) {
                            Text("Réessayer")
                        }
                    }
                }
                state.festivals.isEmpty() -> {
                    Text(
                        "Aucun festival trouvé",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.festivals) { festival ->
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
