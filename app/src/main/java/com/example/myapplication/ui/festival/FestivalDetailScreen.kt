package com.example.myapplication.ui.festival

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.Jeu
import com.example.myapplication.ui.viewmodel.AuthViewModel
import com.example.myapplication.ui.viewmodel.FestivalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FestivalDetailScreen(
    festivalId: Int,
    viewModel: FestivalViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onViewReservations: (Int) -> Unit
) {
    val state by viewModel.detailState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val canReadReservations = authViewModel.canReadReservations()
    val showManagementSections = authState.isLoggedIn

    LaunchedEffect(festivalId) {
        viewModel.loadFestival(festivalId)
        viewModel.loadFestivalGames(festivalId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.festival?.name ?: "Festival") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
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
                    Text(
                        "⚠️ ${state.errorMessage}",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                state.festival != null -> {
                    val festival = state.festival!!
                    val gamesByEditor = state.games.groupBy { it.editeurName ?: "Autres éditeurs" }

                    LazyColumn(
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        festival.name,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.LocationOn,
                                            null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(festival.location)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.CalendarMonth,
                                            null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("${festival.dateDebut.take(10)} → ${festival.dateFin.take(10)}")
                                    }
                                    if (!festival.description.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                        Text(
                                            festival.description,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        if (festival.editeurs.isNotEmpty()) {
                            item {
                                SectionCard(title = "Éditeurs ayant réservé") {
                                    festival.editeurs.forEach { editeur ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Text(
                                                editeur.name,
                                                modifier = Modifier.padding(14.dp),
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (state.games.isNotEmpty()) {
                            item {
                                SectionCard(title = "Jeux présents au festival") {
                                    gamesByEditor.forEach { (editorName, games) ->
                                        Text(
                                            editorName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                                        )
                                        games.forEach { game ->
                                            GameGuestCard(game = game)
                                        }
                                    }
                                }
                            }
                        }

                        if (showManagementSections) {
                            item {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Stock matériel", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        StockRow("Tables standard", festival.stockTablesStandard)
                                        StockRow("Tables grandes", festival.stockTablesGrandes)
                                        StockRow("Tables mairie", festival.stockTablesMairie)
                                        StockRow("Chaises", festival.stockChaises)
                                    }
                                }
                            }

                            if (festival.tariffZones.isNotEmpty()) {
                                item {
                                    Text("Zones tarifaires", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                items(festival.tariffZones) { zone ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(zone.name, fontWeight = FontWeight.SemiBold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "Tables : ${zone.availableTables}/${zone.totalTables} disponibles",
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                "Prix/table : ${zone.pricePerTable}€  ·  Prix/m² : ${zone.pricePerM2}€",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (showManagementSections && canReadReservations) {
                            item {
                                Button(
                                    onClick = { onViewReservations(festival.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Text("Voir les réservations")
                                }
                            }
                        } else if (!authState.isLoggedIn) {
                            item {
                                OutlinedButton(
                                    onClick = onBack,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Text("Retour au catalogue")
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
private fun GameGuestCard(game: Jeu) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(game.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            val details = buildList {
                if (!game.auteurs.isNullOrBlank()) add(game.auteurs)
                if (game.ageMin != null) add("${game.ageMin}+ ans")
            }.joinToString(" · ")
            if (details.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    details,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
            Text(
                "Quantité: ${game.quantite ?: 0}, Tables utilisées: ${game.tablesUtilisees ?: 0}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StockRow(label: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("$value", fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
