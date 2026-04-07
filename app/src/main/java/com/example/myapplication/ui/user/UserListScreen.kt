package com.example.myapplication.ui.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.CreateUserInput
import com.example.myapplication.model.UpdateUserRoleInput
import com.example.myapplication.model.User
import com.example.myapplication.ui.viewmodel.AuthViewModel
import com.example.myapplication.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val state by userViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<User?>(null) }
    var deletingUser by remember { mutableStateOf<User?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        userViewModel.loadUsers()
    }
    
    LaunchedEffect(state.actionSuccessMessage, state.actionErrorMessage) {
        if (state.actionSuccessMessage != null) {
            snackbarHostState.showSnackbar(state.actionSuccessMessage!!)
            userViewModel.clearMessages()
        }
        if (state.actionErrorMessage != null) {
            snackbarHostState.showSnackbar(state.actionErrorMessage!!)
            userViewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestion des Utilisateurs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un utilisateur")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.errorMessage != null && state.users.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Erreur : ${state.errorMessage}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { userViewModel.loadUsers() }) {
                            Text("Réessayer")
                        }
                    }
                }
                state.users.isEmpty() -> {
                    Text("Aucun utilisateur trouvé.", modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.users) { user ->
                            UserCard(
                                user = user,
                                onEditRole = { editingUser = user },
                                onDelete = { deletingUser = user }
                            )
                        }
                    }
                }
            }
            
            if (state.isActionInProgress) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
    
    if (showCreateDialog) {
        CreateUserDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { email, password, role ->
                userViewModel.createUser(CreateUserInput(email, password, role))
                showCreateDialog = false
            }
        )
    }
    
    editingUser?.let { user ->
        EditUserRoleDialog(
            user = user,
            onDismiss = { editingUser = null },
            onConfirm = { newRole ->
                userViewModel.updateUserRole(user.id, UpdateUserRoleInput(newRole))
                editingUser = null
            }
        )
    }
    
    deletingUser?.let { user ->
        AlertDialog(
            onDismissRequest = { deletingUser = null },
            title = { Text("Supprimer l'utilisateur") },
            text = { Text("Êtes-vous sûr de vouloir supprimer l'utilisateur ${user.login} ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userViewModel.deleteUser(user.id)
                        deletingUser = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingUser = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun UserCard(user: User, onEditRole: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.login, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                AssistChip(onClick = {}, label = { Text(user.role) })
            }
            Row {
                IconButton(onClick = onEditRole) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifier le rôle", tint = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun CreateUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("organisateur") }
    
    val roles = listOf("organisateur", "super_organisateur", "super_admin")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvel Utilisateur") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Identifiant (login)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Rôle :", modifier = Modifier.padding(top = 8.dp), fontWeight = FontWeight.Bold)
                roles.forEach { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { role = r }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (role == r),
                            onClick = { role = r }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(r)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        onConfirm(email.trim(), password.trim(), role)
                    }
                },
                enabled = email.isNotBlank() && password.isNotBlank()
            ) {
                Text("Créer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun EditUserRoleDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedRole by remember { mutableStateOf(user.role) }
    val roles = listOf("organisateur", "super_organisateur", "super_admin")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier le Rôle") },
        text = {
            Column {
                Text("Utilisateur : ${user.login}", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                roles.forEach { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRole = r }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedRole == r),
                            onClick = { selectedRole = r }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(r)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedRole) }) { Text("Confirmer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
