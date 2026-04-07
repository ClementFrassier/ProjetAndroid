package com.example.myapplication.ui.festival

// Imports nécessaires pour le fond dégradé et les clics
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

// LazyColumn = liste scrollable optimisée (n'affiche que ce qui est visible à l'écran)
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

// Icônes Material utilisées dans la barre d'actions et les cartes
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
import androidx.compose.material.icons.filled.People

// Composants UI Material 3
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Modèle de données Festival (correspond au JSON renvoyé par le backend)
import com.example.myapplication.model.Festival

// ViewModels : contiennent la logique métier (appels API, états)
import com.example.myapplication.ui.viewmodel.AuthViewModel
import com.example.myapplication.ui.viewmodel.FestivalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FestivalListScreen(
    festivalViewModel: FestivalViewModel,   // Gère la liste des festivals (chargement, état)
    authViewModel: AuthViewModel,           // Gère la session utilisateur (rôle, connexion)
    onFestivalClick: (Int) -> Unit,         // Callback : navigue vers le détail du festival (avec son ID)
    onLogout: () -> Unit,                   // Callback : déconnexion → retour à LoginScreen
    onLogin: () -> Unit,                    // Callback : navigation vers LoginScreen
    onNavigateToEditors: () -> Unit = {},   // Callback : navigation vers la liste des éditeurs
    onNavigateToGames: () -> Unit = {},     // Callback : navigation vers la liste des jeux
    onNavigateToCreateFestival: () -> Unit = {}, // Callback : navigation vers la création de festival
    onNavigateToUsers: () -> Unit = {}      // Callback : navigation vers la gestion des utilisateurs (super_admin uniquement)
) {
    // Observe l'état de la liste des festivals depuis le ViewModel
    // "state" se met à jour automatiquement quand le ViewModel change → l'écran se redessine
    val state by festivalViewModel.listState.collectAsState()

    // Observe l'état de l'authentification (est-on connecté ? quel rôle ?)
    val authState by authViewModel.uiState.collectAsState()

    // Vérifie si l'utilisateur connecté a le droit de créer/modifier des festivals
    // (super_admin ou super_organisateur uniquement)
    val canManageFestivals = authViewModel.canManageFestivals()

    // États locaux de la barre de recherche et des filtres
    var searchName by remember { mutableStateOf("") }         // Texte saisi pour filtrer par nom
    var searchCity by remember { mutableStateOf("") }         // Texte saisi pour filtrer par ville
    var filterDate by remember { mutableStateOf<String?>(null) } // Date sélectionnée ("YYYY-MM-DD") ou null = pas de filtre
    var sortOption by remember { mutableStateOf("date") }     // Critère de tri : "date", "nom" ou "ville"
    var showFilters by remember { mutableStateOf(false) }     // Affiche/cache le panneau de filtres
    var expandedSort by remember { mutableStateOf(false) }    // Ouvre/ferme le menu déroulant de tri
    var showDatePicker by remember { mutableStateOf(false) }  // Affiche/cache le sélecteur de date

    // État du DatePicker Material3 (garde en mémoire la date sélectionnée en millisecondes)
    val datePickerState = rememberDatePickerState()

    // LaunchedEffect(Unit) : exécuté UNE SEULE FOIS au premier affichage de l'écran
    // Déclenche le chargement de la liste → appelle GET /api/festivals via le ViewModel
    LaunchedEffect(Unit) {
        festivalViewModel.loadFestivals()
    }

    // Calcul de la liste filtrée et triée
    // "remember(...)" recalcule UNIQUEMENT quand une des variables entre parenthèses change
    // → évite de refiltrer à chaque recomposition si rien n'a changé (optimisation)
    val filteredFestivals = remember(state.festivals, searchName, searchCity, filterDate, sortOption) {
        state.festivals
            .filter { festival ->
                // Un festival passe le filtre si TOUS les critères sont satisfaits
                val matchName = searchName.isBlank() || festival.name.contains(searchName, ignoreCase = true)
                val matchCity = searchCity.isBlank() || festival.location.contains(searchCity, ignoreCase = true)
                // Pour le filtre date : le festival doit être en cours à la date sélectionnée
                // (dateDebut <= filterDate <= dateFin)
                val matchDate = filterDate == null ||
                    (festival.dateDebut.take(10) <= filterDate!! && festival.dateFin.take(10) >= filterDate!!)
                matchName && matchCity && matchDate
            }
            // Tri selon le critère sélectionné
            .sortedWith(when (sortOption) {
                "nom"   -> compareBy { it.name.lowercase() }         // A → Z par nom
                "ville" -> compareBy { it.location.lowercase() }     // A → Z par ville
                else    -> compareByDescending { it.dateDebut }      // Plus récent en premier (tri par date)
            })
    }

    // ── DIALOGUE DE SÉLECTION DE DATE ──────────────────────────────────────
    // Affiché uniquement quand showDatePicker = true
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false }, // Ferme si l'utilisateur clique dehors
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis // Timestamp de la date choisie
                    if (millis != null) {
                        // Conversion du timestamp en "YYYY-MM-DD" pour comparer avec les dates du backend
                        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                        cal.timeInMillis = millis
                        val y = cal.get(java.util.Calendar.YEAR)
                        val m = cal.get(java.util.Calendar.MONTH) + 1 // Les mois commencent à 0 en Java
                        val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
                        filterDate = "%04d-%02d-%02d".format(y, m, d) // Formatage "2024-03-15"
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = datePickerState) // Calendrier interactif Material3
        }
    }

    // ── STRUCTURE PRINCIPALE : Scaffold ────────────────────────────────────
    // Scaffold gère automatiquement la TopAppBar, le FAB et le contenu principal
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        // ── BARRE DU HAUT ────────────────────────────────────────────────
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Festivals AWI", style = MaterialTheme.typography.titleLarge)
                        // Affiche le login et le rôle de l'utilisateur connecté (sous le titre)
                        if (authState.userLogin != null) {
                            Text(
                                "${authState.userLogin} · ${authState.userRole}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Boutons d'action affichés SEULEMENT si l'utilisateur est connecté
                    if (authState.isLoggedIn) {
                        // Bouton utilisateurs : visible uniquement pour le super_admin
                        if (authState.userRole == "super_admin") {
                            IconButton(onClick = onNavigateToUsers) {
                                Icon(Icons.Filled.People, contentDescription = "Utilisateurs")
                            }
                        }
                        // Bouton catalogue de jeux (visible pour tous les connectés)
                        IconButton(onClick = onNavigateToGames) {
                            Icon(Icons.Filled.Casino, contentDescription = "Jeux")
                        }
                        // Bouton liste des éditeurs
                        IconButton(onClick = onNavigateToEditors) {
                            Icon(Icons.Filled.Business, contentDescription = "Éditeurs")
                        }
                        // Bouton déconnexion : appelle logout() dans AuthViewModel puis navigue
                        IconButton(onClick = {
                            authViewModel.logout()
                            onLogout()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Déconnexion")
                        }
                    } else {
                        // Si non connecté → bouton "Se connecter"
                        TextButton(onClick = onLogin) {
                            Text("Se connecter")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },

        // ── BOUTON FLOTTANT (+) ──────────────────────────────────────────
        // Affiché uniquement si l'utilisateur a le rôle pour créer des festivals
        floatingActionButton = {
            if (authState.isLoggedIn && canManageFestivals) {
                FloatingActionButton(
                    onClick = onNavigateToCreateFestival, // Navigue vers CreateFestivalScreen
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Créer un festival")
                }
            }
        }
    ) { padding -> // "padding" = espace réservé par la TopAppBar (évite que le contenu passe dessous)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    // Fond dégradé subtil : neutre → légèrement teinté → neutre
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(padding) // Applique le padding du Scaffold pour ne pas passer sous la TopAppBar
        ) {

            // ── BANDEAU D'EN-TÊTE ──────────────────────────────────────────
            // Surface colorée avec titre et sous-titre, différents selon si l'on est connecté ou non
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 8.dp),
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.primary, // Fond de la couleur primaire du thème
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Titre différent selon le statut de connexion
                    Text(
                        text = if (authState.isLoggedIn) "Pilotage des festivals"
                               else "Catalogue public des festivals",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    // Sous-titre contextuel
                    Text(
                        text = if (authState.isLoggedIn)
                            "Recherche rapide, tri par ville, et accès direct aux espaces de gestion."
                        else
                            "Explore les événements, consulte les jeux présents et entre en mode invité sans friction.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
                    )
                }
            }

            // ── BARRE DE RECHERCHE PAR NOM ──────────────────────────────────
            // Filtre en temps réel : chaque frappe recalcule "filteredFestivals"
            OutlinedTextField(
                value = searchName,
                onValueChange = { searchName = it }, // Met à jour le filtre à chaque caractère
                placeholder = { Text("Rechercher par nom de festival...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Rechercher") },
                trailingIcon = {
                    Row {
                        // Croix pour effacer le texte (visible seulement si le champ n'est pas vide)
                        if (searchName.isNotEmpty()) {
                            IconButton(onClick = { searchName = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Effacer")
                            }
                        }
                        // Bouton entonnoir pour afficher/cacher le panneau de filtres avancés
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(
                                Icons.Filled.FilterList,
                                contentDescription = "Trier/Filtrer",
                                // Change de couleur si un filtre est actif → indique visuellement qu'un filtre est en cours
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
                singleLine = true,
                shape = RoundedCornerShape(18.dp)
            )

            // ── PANNEAU DE FILTRES AVANCÉS (dépliable) ─────────────────────
            // Affiché uniquement quand showFilters = true (clic sur l'entonnoir)
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
                        // ── FILTRE PAR VILLE ─────────────────────────────────
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
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp)
                        )

                        // ── FILTRE PAR DATE ──────────────────────────────────
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                // Champ en lecture seule (disabled) car la date se choisit via le DatePicker
                                OutlinedTextField(
                                    value = filterDate ?: "",   // Affiche la date sélectionnée ou vide
                                    onValueChange = {},         // Pas de frappe manuelle possible
                                    readOnly = true,
                                    enabled = false,            // Désactivé pour bloquer la frappe clavier
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
                                    // Personnalisation des couleurs pour le champ désactivé
                                    // (par défaut Material3 rend les champs disabled grisés)
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = if (filterDate != null)
                                            MaterialTheme.colorScheme.primary // Bordure bleue si filtre actif
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
                                // Overlay invisible par-dessus le champ désactivé
                                // → capture les clics pour ouvrir le DatePicker
                                // (le champ disabled ne répond pas aux clics, donc on superpose un Box cliquable)
                                Box(
                                    modifier = Modifier
                                        .matchParentSize() // Même taille que le champ
                                        .clickable { showDatePicker = true }
                                )
                            }
                            // Croix pour effacer le filtre date, positionnée à côté du champ
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

                        // ── OPTIONS DE TRI ───────────────────────────────────
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Trier par :", fontWeight = FontWeight.Medium)
                            // ExposedDropdownMenuBox = menu déroulant Material3
                            ExposedDropdownMenuBox(
                                expanded = expandedSort,
                                onExpandedChange = { expandedSort = !expandedSort },
                                modifier = Modifier.weight(1f)
                            ) {
                                // Champ affichant le critère de tri actuel
                                OutlinedTextField(
                                    value = when (sortOption) {
                                        "nom"   -> "Nom"
                                        "ville" -> "Ville"
                                        else    -> "Date"
                                    },
                                    onValueChange = {},
                                    readOnly = true, // Lecture seule : le tri se choisit dans le menu
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSort) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                // Items du menu déroulant
                                ExposedDropdownMenu(
                                    expanded = expandedSort,
                                    onDismissRequest = { expandedSort = false }
                                ) {
                                    // Génère 3 options : Date, Nom, Ville
                                    listOf("date" to "Date", "nom" to "Nom", "ville" to "Ville").forEach { (key, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                sortOption = key  // Met à jour le critère de tri
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

            // ── ZONE D'AFFICHAGE PRINCIPALE ─────────────────────────────────
            Box(modifier = Modifier.fillMaxSize()) {
                // "when" = structure de contrôle (switch en Java) basée sur l'état
                when {
                    // Cas 1 : Chargement en cours → spinner centré
                    state.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    // Cas 2 : Erreur réseau → message + bouton "Réessayer"
                    state.errorMessage != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⚠️ ${state.errorMessage}", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(12.dp))
                            // Réessaye de charger les festivals
                            Button(onClick = { festivalViewModel.loadFestivals() }) {
                                Text("Réessayer")
                            }
                        }
                    }

                    // Cas 3 : Aucun résultat après filtrage → message contextuel
                    filteredFestivals.isEmpty() -> {
                        val hasFilter = searchName.isNotBlank() || searchCity.isNotBlank()
                        Text(
                            if (!hasFilter) "Aucun festival trouvé"        // Pas de festival en base
                            else "Aucun festival correspondant à ces critères", // Filtre trop restrictif
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Cas 4 : Liste disponible → affichage dans une liste scrollable optimisée
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),   // Padding autour de la liste
                            verticalArrangement = Arrangement.spacedBy(12.dp) // Espace entre les cartes
                        ) {
                            // "items" génère un élément par festival, "key" optimise les recompositions
                            items(filteredFestivals, key = { it.id }) { festival ->
                                FestivalCard(
                                    festival = festival,
                                    onClick = { onFestivalClick(festival.id) } // Navigue vers le détail
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── COMPOSANT FestivalCard ──────────────────────────────────────────────────
// Composant privé (utilisé uniquement dans ce fichier) qui affiche une carte de festival
@Composable
private fun FestivalCard(festival: Festival, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // Toute la carte est cliquable
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ),
        elevation = CardDefaults.cardElevation(8.dp) // Ombre pour effet "surélevé"
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Nom du festival en titre
            Text(
                text = festival.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Localisation avec icône
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

            // Dates de début et de fin
            // ".take(10)" : on garde seulement "YYYY-MM-DD" (le backend renvoie parfois "YYYY-MM-DDTHH:mm:ssZ")
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

            // Description (affichée seulement si elle n'est pas vide)
            if (!festival.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    festival.description,
                    fontSize = 13.sp,
                    maxLines = 2,  // Tronque à 2 lignes pour garder la carte compacte
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Puces d'information (chips) : nombre de tables et nombre de zones tarifaires
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Chip "X tables"
                AssistChip(
                    onClick = {}, // Pas d'action au clic (juste informatif)
                    label = { Text("${festival.totalTables} tables") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
                // Chip "X zones" affiché seulement si des zones tarifaires existent
                if (festival.tariffZones.isNotEmpty()) {
                    AssistChip(
                        onClick = {},
                        label = { Text("${festival.tariffZones.size} zones") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    }
}
