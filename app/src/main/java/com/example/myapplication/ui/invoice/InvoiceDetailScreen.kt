package com.example.myapplication.ui.invoice

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.InvoiceCreateInput
import com.example.myapplication.ui.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoiceId: Int?,
    reservationId: Int?, // Passé lors de la création d'une facture depuis une réservation
    viewModel: InvoiceViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.detailState.collectAsState()
    val context = LocalContext.current

    var issueDate by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(invoiceId) {
        if (invoiceId != null) {
            viewModel.loadInvoice(invoiceId)
        } else {
            viewModel.resetDetailState()
            // Default dates for new invoice (Very basic string manipulation for demo)
            val today = java.time.LocalDate.now()
            issueDate = today.toString() + "T00:00:00Z"
            dueDate = today.plusDays(30).toString() + "T00:00:00Z"
        }
    }

    LaunchedEffect(state.operationSuccess) {
        if (state.operationSuccess && (invoiceId == null || state.isDeleting)) {
            // Création ou suppression réussie, on retourne en arrière
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (invoiceId == null) "Nouvelle Facture" else "Aperçu Facture", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                actions = {
                    if (invoiceId != null && state.invoice?.isPaid == false) {
                        IconButton(onClick = { viewModel.deleteInvoice(invoiceId) }) {
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
        if (state.isLoading) {
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

                if (invoiceId == null) {
                    // Creation Form
                    item {
                        OutlinedTextField(
                            value = issueDate,
                            onValueChange = { issueDate = it },
                            label = { Text("Date d'émission (ISO 8601)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            label = { Text("Date d'échéance (ISO 8601)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes optionnelles") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                    item {
                        Button(
                            onClick = {
                                if (reservationId != null) {
                                    viewModel.createInvoice(
                                        InvoiceCreateInput(
                                            reservationId = reservationId,
                                            issueDate = issueDate,
                                            dueDate = dueDate,
                                            notes = notes.takeIf { it.isNotBlank() }
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            enabled = !state.isSaving && reservationId != null
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Générer la Facture")
                            }
                        }
                    }
                } else {
                    // View Mode
                    state.invoice?.let { invoice ->
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Facture N° ${invoice.invoiceNumber}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Émise le : ${invoice.issueDate.substringBefore("T")}")
                                    Text("Échéance : ${invoice.dueDate.substringBefore("T")}")
                                    Text("Montant Total : ${invoice.amount}€", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        item {
                            if (!invoice.isPaid) {
                                Button(
                                    onClick = { viewModel.markInvoiceAsPaid(invoiceId) },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    enabled = !state.isPaying
                                ) {
                                    if (state.isPaying) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                    } else {
                                        Text("Marquer comme Payée")
                                    }
                                }
                            } else {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                ) {
                                    Text(
                                        "✅ Facture Payée le ${invoice.paidAt?.substringBefore("T") ?: "N/A"}",
                                        modifier = Modifier.padding(16.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                        
                        // Fake Download PDF functionality
                        item {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://backend.api/invoices/${invoiceId}/pdf"))
                                    // In a real app, this would trigger a download manager or open browser with Auth headers
                                    // Context would be needed for the intent
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Télécharger le PDF")
                            }
                        }
                        
                        if (!invoice.notes.isNullOrBlank()) {
                            item {
                                Text("Notes :", fontWeight = FontWeight.Bold)
                                Text(invoice.notes ?: "")
                            }
                        }

                        invoice.reservation?.let { res ->
                            item { Divider(modifier = Modifier.padding(vertical = 16.dp)) }
                            item {
                                Text("Détails de Réservation :", fontWeight = FontWeight.Bold)
                                Text("Éditeur : ${res.editor?.name ?: "N/A"}")
                                Text("Tables : ${res.totalTables}")
                            }
                        }
                    }
                }
            }
        }
    }
}
