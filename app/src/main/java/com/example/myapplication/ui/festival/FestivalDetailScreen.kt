package com.example.myapplication.ui.festival

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.viewmodel.FestivalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FestivalDetailScreen(
    festivalId: Int,
    viewModel: FestivalViewModel,
    onBack: () -> Unit,
    onViewReservations: (Int) -> Unit
) {
    val state by viewModel.detailState.collectAsState()

    LaunchedEffect(festivalId) {
        viewModel.loadFestival(festivalId)
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
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            // Infos générales
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
                                        Icon(Icons.Filled.LocationOn, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(festival.location)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.CalendarMonth, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("${festival.dateDebut.take(10)} → ${festival.dateFin.take(10)}")
                                    }
                                    if (!festival.description.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(festival.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        item {
                            // Stock de matériel
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
                                        Text("Tables : ${zone.availableTables}/${zone.totalTables} disponibles", fontSize = 13.sp)
                                        Text("Prix/table : ${zone.pricePerTable}€  ·  Prix/m² : ${zone.pricePerM2}€", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

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
                    }
                }
            }
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
