package com.example.myapplication.ui.invoice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    reservationId: Int,
    viewModel: InvoiceViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.detailState.collectAsState()

    LaunchedEffect(reservationId) {
        viewModel.loadInvoiceByReservation(reservationId)
    }

    LaunchedEffect(state.operationSuccess) {
        if (state.operationSuccess && state.invoice != null) {
            viewModel.loadInvoiceByReservation(reservationId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Facture", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
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

                    if (state.invoice == null) {
                        Text(
                            "Aucune facture n'existe encore pour cette réservation.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { viewModel.createInvoice(reservationId) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            enabled = !state.isSaving
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.height(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Créer la facture")
                            }
                        }
                    } else {
                        val invoice = state.invoice!!
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("N° ${invoice.invoiceNumber}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Text("Montant TTC : ${invoice.amount}€")
                                Text("Émise le : ${invoice.issueDate.substringBefore("T")}")
                                Text("Statut : ${invoice.status}")
                                if (invoice.paidAt != null) {
                                    Text("Payée le : ${invoice.paidAt.substringBefore("T")}")
                                }
                            }
                        }

                        if (invoice.status != "payee") {
                            OutlinedButton(
                                onClick = { viewModel.markInvoiceAsPaid(invoice.id) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                enabled = !state.isPaying
                            ) {
                                if (state.isPaying) {
                                    CircularProgressIndicator(modifier = Modifier.height(24.dp))
                                } else {
                                    Text("Marquer comme payée")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
