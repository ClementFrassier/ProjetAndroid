package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val login: String,
    val password: String
)

data class AuthResponse(
    val message: String,
    val user: UserInfo?
)

data class UserInfo(
    val login: String,
    val role: String
)

data class TariffZone(
    val id: Int,
    val name: String,
    val totalTables: Int,
    val availableTables: Int,
    val pricePerTable: Double,
    val pricePerM2: Double
)

data class Festival(
    val id: Int,
    val name: String,
    val location: String,
    val dateDebut: String,
    val dateFin: String,
    val description: String?,
    val totalTables: Int,
    val stockTablesStandard: Int,
    val stockTablesGrandes: Int,
    val stockTablesMairie: Int,
    val stockChaises: Int,
    val tariffZones: List<TariffZone>,
    val editeurs: List<EditeurLight>
)

data class CreateFestivalRequest(
    @SerializedName("nom") val nom: String,
    val location: String,
    @SerializedName("date_debut") val dateDebut: String,
    @SerializedName("date_fin") val dateFin: String,
    val description: String?,
    @SerializedName("nombre_total_tables") val nombreTotalTables: Int
)

data class EditeurLight(
    val id: Int,
    val name: String
)

data class Editor(
    val id: Int,
    val name: String,
    val description: String? = null,
    @SerializedName("type_reservant") val typeReservant: String? = null,
    @SerializedName("est_reservant") val estReservant: Boolean? = null
)

data class EditorDetail(
    val id: Int,
    val name: String,
    val description: String? = null,
    @SerializedName("type_reservant") val typeReservant: String? = null,
    @SerializedName("est_reservant") val estReservant: Boolean? = null,
    val contacts: List<Contact> = emptyList(),
    val games: List<Game> = emptyList()
)

data class Contact(
    val id: Int,
    val fullName: String,
    val email: String?,
    val phone: String?,
    val isPrimary: Boolean
)

data class Game(
    val id: Int,
    val name: String,
    @SerializedName("type") val type: String?,
    @SerializedName("authors") val authors: String? = null,
    @SerializedName("ageMin") val minAge: Int?,
    @SerializedName("ageMax") val maxAge: Int?
)

data class GameWithEditor(
    val id: Int,
    @SerializedName("nom") val name: String,
    @SerializedName("type_jeu") val type: String?,
    @SerializedName("auteurs") val authors: String? = null,
    @SerializedName("age_min") val minAge: Int?,
    @SerializedName("age_max") val maxAge: Int?,
    @SerializedName("editeur_id") val editorId: Int? = null,
    @SerializedName("editeur_name") val editorName: String? = null
)

data class ContactInput(
    val id: Int? = null,
    val fullName: String,
    val email: String? = null,
    val phone: String? = null,
    val isPrimary: Boolean? = null
)

data class GameInput(
    val id: Int? = null,
    @SerializedName("nom") val name: String,
    @SerializedName("auteurs") val authors: String? = null,
    @SerializedName("type_jeu") val type: String? = null,
    @SerializedName("age_min") val minAge: Int? = null,
    @SerializedName("age_max") val maxAge: Int? = null
)

data class GameCreateInput(
    @SerializedName("editeur_id") val editorId: Int,
    @SerializedName("nom") val name: String,
    @SerializedName("auteurs") val authors: String? = null,
    @SerializedName("type_jeu") val type: String? = null,
    @SerializedName("age_min") val minAge: Int? = null,
    @SerializedName("age_max") val maxAge: Int? = null
)

data class EditorInput(
    @SerializedName("nom") val name: String,
    val description: String? = null,
    @SerializedName("type_reservant") val typeReservant: String? = null,
    @SerializedName("est_reservant") val estReservant: Boolean? = null
)

data class Jeu(
    val id: Int,
    val name: String,
    val auteurs: String?,
    val ageMin: Int?,
    val ageMax: Int?,
    val typeJeu: String?,
    val editeurName: String?,
    val editeurId: Int?,
    val quantite: Int?,
    val tablesUtilisees: Int?
)

data class ReservationLine(
    @SerializedName("zone_tarifaire_id") val tariffZoneId: Int,
    @SerializedName("nombre_tables") val tablesCount: Int,
    @SerializedName("surface_m2") val surfaceSquareM: Double? = null,
    @SerializedName("zone_nom") val zoneName: String? = null,
    @SerializedName("prix_table") val pricePerTable: Double? = null,
    @SerializedName("prix_m2") val pricePerSquareM: Double? = null
)

data class Reservation(
    val id: Int,
    @SerializedName("festival_id") val festivalId: Int,
    @SerializedName("editeur_id") val editorId: Int?,
    @SerializedName("tables_totales") val totalTables: Int = 0,
    @SerializedName("prix_total") val subtotalAmount: Double = 0.0,
    @SerializedName("remise_tables_offertes") val discountTables: Int = 0,
    @SerializedName("remise_argent") val discountAmount: Double = 0.0,
    @SerializedName("prix_final") val finalAmount: Double = 0.0,
    @SerializedName("statut_workflow") val workflowState: String,
    @SerializedName("editeur_presente_jeux") val willPresentGames: Boolean = false,
    @SerializedName("prises_electriques") val powerOutlets: Int = 0,
    @SerializedName("notes") val gamesNotes: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("editeur_nom") val editorName: String? = null,
    @SerializedName("lignes") val lines: List<ReservationLine>? = null
)

data class ReservationLineInput(
    @SerializedName("zone_tarifaire_id") val tariffZoneId: Int,
    @SerializedName("nombre_tables") val tablesCount: Int = 0,
    @SerializedName("surface_m2") val surfaceM2: Double? = null
)

data class ZonePlan(
    val id: Int,
    @SerializedName("festival_id") val festivalId: Int,
    @SerializedName("zone_tarifaire_id") val tariffZoneId: Int,
    @SerializedName("nom") val name: String,
    @SerializedName("nombre_tables") val tablesCount: Int
)

data class ZonePlanInput(
    @SerializedName("festival_id") val festivalId: Int,
    @SerializedName("zone_tarifaire_id") val tariffZoneId: Int,
    @SerializedName("nom") val name: String,
    @SerializedName("nombre_tables") val tablesCount: Int
)

data class ZonePlanUpdateInput(
    @SerializedName("festival_id") val festivalId: Int? = null,
    @SerializedName("zone_tarifaire_id") val tariffZoneId: Int? = null,
    @SerializedName("nom") val name: String? = null,
    @SerializedName("nombre_tables") val tablesCount: Int? = null
)

data class ReservationGamePlacement(
    val id: Int,
    @SerializedName("jeu_id") val gameId: Int,
    @SerializedName("reservation_id") val reservationId: Int,
    @SerializedName("zone_plan_id") val zonePlanId: Int? = null,
    @SerializedName("quantite") val quantity: Int,
    @SerializedName("nombre_tables_allouees") val allocatedTables: Int? = null,
    @SerializedName("type_table") val tableType: String = "standard",
    @SerializedName("tables_utilisees") val usedTables: Int = 0,
    @SerializedName("liste_demandee") val requestedList: Boolean? = null,
    @SerializedName("liste_obtenue") val obtainedList: Boolean? = null,
    @SerializedName("jeux_recus") val receivedGames: Boolean? = null,
    @SerializedName("nom_jeu") val gameName: String? = null,
    @SerializedName("type_jeu") val gameType: String? = null,
    @SerializedName("nom_zone") val zoneName: String? = null,
    @SerializedName("festival_id") val festivalId: Int? = null
)

data class ReservationGamePlacementCreateInput(
    @SerializedName("jeu_id") val gameId: Int,
    @SerializedName("reservation_id") val reservationId: Int,
    @SerializedName("zone_plan_id") val zonePlanId: Int? = null,
    @SerializedName("quantite") val quantity: Int,
    @SerializedName("nombre_tables_allouees") val allocatedTables: Int? = null,
    @SerializedName("type_table") val tableType: String = "standard",
    @SerializedName("tables_utilisees") val usedTables: Int,
    @SerializedName("liste_demandee") val requestedList: Boolean = false,
    @SerializedName("liste_obtenue") val obtainedList: Boolean = false,
    @SerializedName("jeux_recus") val receivedGames: Boolean = false
)

data class ReservationGamePlacementUpdateInput(
    @SerializedName("zone_plan_id") val zonePlanId: Int? = null,
    @SerializedName("quantite") val quantity: Int? = null,
    @SerializedName("nombre_tables_allouees") val allocatedTables: Int? = null,
    @SerializedName("type_table") val tableType: String? = null,
    @SerializedName("tables_utilisees") val usedTables: Int? = null,
    @SerializedName("liste_demandee") val requestedList: Boolean? = null,
    @SerializedName("liste_obtenue") val obtainedList: Boolean? = null,
    @SerializedName("jeux_recus") val receivedGames: Boolean? = null
)

data class ReservationCreateInput(
    @SerializedName("festival_id") val festivalId: Int,
    @SerializedName("editeur_id") val editorId: Int? = null,
    @SerializedName("editeur_presente_jeux") val willPresentGames: Boolean? = null,
    @SerializedName("prises_electriques") val powerOutlets: Int? = null,
    @SerializedName("remise_tables_offertes") val discountTables: Int? = null,
    @SerializedName("remise_argent") val discountAmount: Double? = null,
    @SerializedName("notes") val gamesNotes: String? = null,
    @SerializedName("lignes") val lines: List<ReservationLineInput>
)

data class ReservationUpdateInput(
    @SerializedName("editeur_presente_jeux") val willPresentGames: Boolean? = null,
    @SerializedName("remise_tables_offertes") val discountTables: Int? = null,
    @SerializedName("remise_argent") val discountAmount: Double? = null,
    @SerializedName("prises_electriques") val powerOutlets: Int? = null,
    @SerializedName("notes") val gamesNotes: String? = null,
    @SerializedName("statut_workflow") val workflowState: String? = null,
    @SerializedName("lignes") val lines: List<ReservationLineInput>? = null
)

data class FestivalLight(
    val id: Int,
    val name: String
)

data class Invoice(
    val id: Int,
    @SerializedName("reservation_id") val reservationId: Int,
    @SerializedName("numero") val invoiceNumber: String,
    @SerializedName("montant_ttc") val amount: Double,
    @SerializedName("statut") val status: String,
    @SerializedName("emise_le") val issueDate: String,
    @SerializedName("payee_le") val paidAt: String? = null
)

data class InvoiceResponse(
    val message: String,
    val facture: Invoice
)

data class FestivalResponse(
    val message: String,
    val festival: Festival
)

data class EditorResponse(
    val message: String,
    val editeur: EditorDetail
)

data class GameResponse(
    val message: String,
    val jeu: GameWithEditor
)

data class ReservationResponse(
    val message: String,
    val reservation: Reservation
)

data class ZonePlanResponse(
    val message: String,
    @SerializedName("zone_plan") val zonePlan: ZonePlan
)

data class ReservationGamePlacementResponse(
    val message: String,
    @SerializedName("jeu_festival") val reservationGamePlacement: ReservationGamePlacement? = null,
    @SerializedName("data") val updatedPlacement: ReservationGamePlacement? = null
)

// USERS
data class User(
    val id: Int,
    val email: String,
    val role: String,
    val createdAt: String
)

data class CreateUserInput(
    val email: String,
    val password: String,
    val role: String
)

data class UpdateUserRoleInput(
    val role: String
)
