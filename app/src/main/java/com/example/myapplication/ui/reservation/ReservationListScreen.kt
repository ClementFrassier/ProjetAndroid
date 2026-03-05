package com.example.myapplication.ui.reservation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.Reservation
import com.example.myapplication.ui.viewmodel.ReservationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationListScreen(
    festivalId: Int,
    viewModel: ReservationViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(festivalId) {
        viewModel.loadReservationsByFestival(festivalId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Réservations") },
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
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⚠️ ${state.errorMessage}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadReservationsByFestival(festivalId) }) {
                            Text("Réessayer")
                        }
                    }
                }
                state.reservations.isEmpty() -> {
                    Text(
                        "Aucune réservation pour ce festival",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                "${state.reservations.size} réservation(s)",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(state.reservations) { reservation ->
                            ReservationCard(reservation)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationCard(reservation: Reservation) {
    val statusColor = when (reservation.statutWorkflow) {
        "present" -> MaterialTheme.colorScheme.primary
        "facture" -> MaterialTheme.colorScheme.tertiary
        "facture_payee" -> MaterialTheme.colorScheme.secondary
        "annulée" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    reservation.editeurNom ?: "Éditeur #${reservation.editeurId}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Badge(containerColor = statusColor) {
                    Text(
                        reservation.statutWorkflow ?: "—",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (reservation.tablesTotales != null) {
                    InfoChipText("${reservation.tablesTotales} tables")
                }
                if (reservation.prixFinal != null) {
                    InfoChipText("${reservation.prixFinal}€")
                }
            }

            if (reservation.notes?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    reservation.notes,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            // Détails de la réservation
            val extras = buildList {
                if (reservation.editeurPresenTeJeux == true) add("Présente des jeux")
                if (reservation.besoinAnimateur == true) add("Besoin animateur")
                if ((reservation.prisesElectriques ?: 0) > 0) add("${reservation.prisesElectriques} prise(s) élec.")
            }
            if (extras.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(extras.joinToString("  ·  "), fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Composable
private fun InfoChipText(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 13.sp
        )
    }
}
