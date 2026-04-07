package com.example.myapplication.ui.auth

// Imports des composants de mise en page et d'affichage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions

// Imports des icônes Material utilisées dans les champs
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

// Imports des composants Material 3 (UI Design System Google)
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Import du ViewModel qui gère la logique d'authentification
import com.example.myapplication.ui.viewmodel.AuthViewModel

// @OptIn nécessaire pour utiliser ExposedDropdownMenu et autres APIs expérimentales Material3
@OptIn(ExperimentalMaterial3Api::class)
// @Composable indique que cette fonction est un écran Jetpack Compose (pas une activité classique)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,          // Le ViewModel qui contient la logique login/logout
    onLoginSuccess: () -> Unit,        // Callback appelé quand la connexion réussit → navigue vers l'écran suivant
    onContinueWithoutLogin: () -> Unit // Callback si l'utilisateur veut accéder sans compte
) {
    // Observe l'état de l'UI depuis le ViewModel. "by" délègue la valeur automatiquement.
    // uiState contient : isLoggedIn, isLoading, errorMessage, userLogin, userRole
    val uiState by viewModel.uiState.collectAsState()

    // État local du champ "identifiant" (géré dans le Composable, remis à zéro à chaque recomposition si non mémorisé)
    var login by remember { mutableStateOf("") }

    // État local du champ "mot de passe"
    var password by remember { mutableStateOf("") }

    // État local pour afficher ou masquer le mot de passe (œil)
    var passwordVisible by remember { mutableStateOf(false) }

    // LaunchedEffect : bloc exécuté en coroutine quand "uiState.isLoggedIn" change
    // Si l'utilisateur vient de se connecter → on appelle le callback de navigation
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoginSuccess()
    }

    // Box = conteneur qui empile ses enfants. Ici il prend tout l'écran (fillMaxSize)
    Box(
        modifier = Modifier
            .fillMaxSize() // Occupe toute la taille de l'écran
            .background(
                // Dégradé vertical : couleur primaire en haut, fond neutre vers le bas
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f), // Teinté en haut
                        MaterialTheme.colorScheme.background,  // Neutre au milieu
                        MaterialTheme.colorScheme.background   // Neutre en bas
                    )
                )
            ),
        contentAlignment = Alignment.Center // Centre le contenu verticalement et horizontalement
    ) {
        // Column = disposition verticale des éléments
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp), // Marges gauche/droite pour ne pas coller aux bords
            horizontalAlignment = Alignment.CenterHorizontally, // Centre horizontalement
            verticalArrangement = Arrangement.spacedBy(20.dp)  // Espace de 20dp entre chaque élément
        ) {

            // ── SECTION EN-TÊTE (icône + titre + sous-titre) ──────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Icône ronde avec emoji festival
                Surface(
                    shape = RoundedCornerShape(24.dp), // Coins très arrondis = carré arrondi
                    color = MaterialTheme.colorScheme.primary, // Couleur primaire du thème
                    modifier = Modifier.size(72.dp) // Taille fixe 72x72dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Emoji affiché au centre du carré
                        Text("🎪", fontSize = 36.sp)
                    }
                }

                // Titre principal de l'application
                Text(
                    text = "AWI Festival",
                    style = MaterialTheme.typography.headlineLarge, // Typographie grande
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Sous-titre descriptif, centré
                Text(
                    text = "Connectez-vous pour gérer les festivals",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Gris discret
                    textAlign = TextAlign.Center
                )
            }

            // ── SECTION CARTE DE CONNEXION ─────────────────────────────────────────
            // Card = surface élevée (ombre), idéale pour regrouper un formulaire
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp), // Coins très arrondis
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface // Fond blanc/gris clair selon le thème
                ),
                elevation = CardDefaults.cardElevation(8.dp) // Ombre portée
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp), // Padding interne de la carte
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Espace entre les champs
                ) {

                    // ── CHAMP IDENTIFIANT ─────────────────────────────────────────
                    OutlinedTextField(
                        value = login,                          // Valeur actuelle du champ
                        onValueChange = {
                            login = it                          // Met à jour l'état local à chaque frappe
                            if (uiState.errorMessage != null) viewModel.clearError() // Efface l'erreur si on retape
                        },
                        label = { Text("Identifiant") },        // Label flottant
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null) // Icône personne à gauche
                        },
                        singleLine = true,                      // Une seule ligne (pas de retour à la ligne)
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.errorMessage != null, // Borde en rouge si erreur
                        shape = RoundedCornerShape(16.dp)       // Coins arrondis du champ
                    )

                    // ── CHAMP MOT DE PASSE ────────────────────────────────────────
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (uiState.errorMessage != null) viewModel.clearError()
                        },
                        label = { Text("Mot de passe") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null) // Icône cadenas
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.errorMessage != null,
                        shape = RoundedCornerShape(16.dp),
                        // Si passwordVisible = true → texte visible, sinon → remplacé par des points
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), // Clavier spécial mot de passe
                        trailingIcon = {
                            // Bouton œil à droite pour afficher/masquer le mot de passe
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Filled.VisibilityOff // Œil barré si visible
                                    else Icons.Filled.Visibility,                    // Œil ouvert si masqué
                                    contentDescription = if (passwordVisible) "Masquer" else "Afficher"
                                )
                            }
                        }
                    )

                    // ── MESSAGE D'ERREUR (affiché seulement si une erreur existe) ──
                    if (uiState.errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer // Fond rouge clair
                        ) {
                            Text(
                                text = "⚠ ${uiState.errorMessage}", // Texte de l'erreur renvoyé par le backend
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // ── BOUTON SE CONNECTER ───────────────────────────────────────
                    Button(
                        onClick = {
                            viewModel.clearError()             // Efface les erreurs précédentes
                            viewModel.login(login, password)   // Déclenche l'appel API POST /api/auth/login
                        },
                        // Bouton désactivé si : chargement en cours, ou champs vides
                        enabled = !uiState.isLoading && login.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        // Affiche un spinner pendant le chargement, sinon le texte du bouton
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text("Se connecter", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // ── SÉPARATEUR "ou" ────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f)) // Ligne à gauche
                Text(
                    "  ou  ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                HorizontalDivider(modifier = Modifier.weight(1f)) // Ligne à droite
            }

            // ── BOUTON CONTINUER SANS CONNEXION ───────────────────────────────────
            // OutlinedButton = bouton avec bordure (moins prioritaire visuellement que Button)
            OutlinedButton(
                onClick = onContinueWithoutLogin, // Navigue en mode invité (accès lecture seule)
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Continuer sans se connecter", fontSize = 15.sp)
            }
        }
    }
}
