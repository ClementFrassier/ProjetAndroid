package com.example.myapplication.ui.crm

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.CrmFollowUp
import com.example.myapplication.model.CrmRow
import com.example.myapplication.ui.viewmodel.AuthViewModel
import com.example.myapplication.ui.viewmodel.CrmViewModel

private val crmStatuses = listOf(
    "pas_de_contact" to "Pas de contact",
    "contact_pris" to "Contact pris",
    "discussion_en_cours" to "Discussion en cours",
    "sera_absent" to "Sera absent",
    "considere_absent" to "Considéré absent",
    "present" to "Présent"
)

private val contactTypes = listOf(
    "email" to "Email",
    "telephone" to "Téléphone",
    "physique" to "Physique",
    "autre" to "Autre"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmScreen(
    festivalId: Int,
    authViewModel: AuthViewModel,
    viewModel: CrmViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val canManageCrm = authViewModel.canManageReservations()
    val notesDrafts = remember { mutableStateMapOf<Int, String>() }
    val contactTypeDrafts = remember { mutableStateMapOf<Int, String>() }
    val contactNotesDrafts = remember { mutableStateMapOf<Int, String>() }
    val historyOpen = remember { mutableStateMapOf<Int, Boolean>() }

    LaunchedEffect(festivalId) {
        viewModel.loadFestivalCrm(festivalId)
    }

    LaunchedEffect(state.rows) {
        state.rows.forEach { row ->
            if (!notesDrafts.containsKey(row.editorId)) {
                notesDrafts[row.editorId] = row.notes.orEmpty()
            }
            if (!contactTypeDrafts.containsKey(row.editorId)) {
                contactTypeDrafts[row.editorId] = "email"
            }
            if (!contactNotesDrafts.containsKey(row.editorId)) {
                contactNotesDrafts[row.editorId] = ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Suivi CRM", fontWeight = FontWeight.Bold) },
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
        when {
            !canManageCrm -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Accès CRM réservé aux gestionnaires.", color = MaterialTheme.colorScheme.error)
                }
            }

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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    state.errorMessage?.let { message ->
                        item {
                            Text("⚠️ $message", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    state.saveErrorMessage?.let { message ->
                        item {
                            Text("⚠️ $message", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    item {
                        Text(
                            "${state.rows.size} éditeur(s) suivis",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(state.rows, key = { it.editorId }) { row ->
                        CrmRowCard(
                            row = row,
                            notesDraft = notesDrafts[row.editorId].orEmpty(),
                            contactTypeDraft = contactTypeDrafts[row.editorId] ?: "email",
                            contactNotesDraft = contactNotesDrafts[row.editorId].orEmpty(),
                            isSaving = state.isSaving,
                            historyOpen = historyOpen[row.editorId] == true,
                            contacts = state.contactsByEditor[row.editorId].orEmpty(),
                            contactsLoading = state.contactsLoading.contains(row.editorId),
                            onStatusSelected = { status ->
                                viewModel.updateStatus(row.editorId, festivalId, status)
                            },
                            onNotesChange = { notesDrafts[row.editorId] = it },
                            onSaveNotes = {
                                viewModel.saveNotes(
                                    row.editorId,
                                    festivalId,
                                    row.status,
                                    notesDrafts[row.editorId]
                                )
                            },
                            onContactTypeChange = { contactTypeDrafts[row.editorId] = it },
                            onContactNotesChange = { contactNotesDrafts[row.editorId] = it },
                            onAddContact = {
                                viewModel.addContact(
                                    row.editorId,
                                    festivalId,
                                    contactTypeDrafts[row.editorId],
                                    contactNotesDrafts[row.editorId]
                                )
                                contactNotesDrafts[row.editorId] = ""
                            },
                            onToggleHistory = {
                                val next = !(historyOpen[row.editorId] == true)
                                historyOpen[row.editorId] = next
                                if (next && !state.contactsByEditor.containsKey(row.editorId)) {
                                    viewModel.loadContacts(row.editorId, festivalId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CrmRowCard(
    row: CrmRow,
    notesDraft: String,
    contactTypeDraft: String,
    contactNotesDraft: String,
    isSaving: Boolean,
    historyOpen: Boolean,
    contacts: List<CrmFollowUp>,
    contactsLoading: Boolean,
    onStatusSelected: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSaveNotes: () -> Unit,
    onContactTypeChange: (String) -> Unit,
    onContactNotesChange: (String) -> Unit,
    onAddContact: () -> Unit,
    onToggleHistory: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(row.editorName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        buildString {
                            append(row.reservantType ?: "éditeur")
                            if (row.isReservant == false) append(" · non réservant")
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(row.status ?: "pas_de_contact")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoPill("Contacts: ${row.totalContacts ?: 0}")
                InfoPill("Dernier: ${row.lastContact?.substringBefore('T') ?: "—"}")
                InfoPill("Relance: ${row.lastFollowUp?.substringBefore('T') ?: "—"}")
            }

            StringDropdownField(
                label = "Statut CRM",
                selected = row.status ?: "pas_de_contact",
                options = crmStatuses,
                enabled = !isSaving,
                onSelect = onStatusSelected
            )

            StringDropdownField(
                label = "Type de contact",
                selected = contactTypeDraft,
                options = contactTypes,
                enabled = !isSaving,
                onSelect = onContactTypeChange
            )

            OutlinedTextField(
                value = contactNotesDraft,
                onValueChange = onContactNotesChange,
                label = { Text("Note rapide de contact") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onAddContact,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ajouter contact")
            }

            HorizontalDivider()

            OutlinedTextField(
                value = notesDraft,
                onValueChange = onNotesChange,
                label = { Text("Notes CRM") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onSaveNotes,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Enregistrer les notes")
                }
                OutlinedButton(
                    onClick = onToggleHistory,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (historyOpen) "Masquer l'historique" else "Historique")
                }
            }

            if (historyOpen) {
                if (contactsLoading) {
                    Text("Chargement de l'historique...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (contacts.isEmpty()) {
                    Text("Aucun contact enregistré.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    contacts.forEach { contact ->
                        ContactHistoryCard(contact)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactHistoryCard(contact: CrmFollowUp) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(contact.contactDate.substringBefore("T"), fontWeight = FontWeight.SemiBold)
            if (!contact.contactType.isNullOrBlank()) {
                Text(contact.contactType, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!contact.notes.isNullOrBlank()) {
                Text(contact.notes)
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val label = crmStatuses.find { it.first == status }?.second ?: status
    val color = when (status) {
        "contact_pris" -> MaterialTheme.colorScheme.primary
        "discussion_en_cours" -> MaterialTheme.colorScheme.tertiary
        "present" -> MaterialTheme.colorScheme.secondary
        "sera_absent", "considere_absent" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }
    Surface(color = color, shape = MaterialTheme.shapes.small) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun InfoPill(text: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp)
    }
}

@Composable
private fun StringDropdownField(
    label: String,
    selected: String,
    options: List<Pair<String, String>>,
    enabled: Boolean,
    onSelect: (String) -> Unit
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
            Text(options.find { it.first == selected }?.second ?: selected)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    }
                )
            }
        }
    }
}
