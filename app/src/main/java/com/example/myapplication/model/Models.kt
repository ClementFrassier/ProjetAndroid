package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val login: String,
    val password: String
)

data class AuthResponse(
    val message: String,
    val user: UserInfo?,
    val accessToken: String? = null
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

data class EditeurLight(
    val id: Int,
    val name: String
)

data class Editeur(
    val id: Int,
    val nom: String,
    val email: String?,
    val telephone: String?,
    val adresse: String?,
    val siret: String?
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
    @SerializedName("zone_tarifaire_id") val zoneTarifaireId: Int,
    @SerializedName("nombre_tables") val nombreTables: Int,
    @SerializedName("surface_m2") val surfaceM2: Double?,
    @SerializedName("zone_nom") val zoneNom: String?,
    @SerializedName("prix_table") val prixTable: Double?,
    @SerializedName("prix_m2") val prixM2: Double?
)

data class Reservation(
    val id: Int,
    @SerializedName("editeur_id") val editeurId: Int,
    @SerializedName("festival_id") val festivalId: Int,
    @SerializedName("editeur_nom") val editeurNom: String?,
    @SerializedName("prix_total") val prixTotal: Double?,
    @SerializedName("prix_final") val prixFinal: Double?,
    @SerializedName("statut_workflow") val statutWorkflow: String?,
    @SerializedName("remise_tables_offertes") val remiseTablesOffertes: Int?,
    @SerializedName("remise_argent") val remiseArgent: Double?,
    @SerializedName("editeur_presente_jeux") val editeurPresenTeJeux: Boolean?,
    @SerializedName("besoin_animateur") val besoinAnimateur: Boolean?,
    @SerializedName("prises_electriques") val prisesElectriques: Int?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("souhait_grandes_tables") val souhaitGrandesTables: Int?,
    @SerializedName("souhait_tables_standard") val souhaitTablesStandard: Int?,
    @SerializedName("souhait_tables_mairie") val souhaitTablesMairie: Int?,
    @SerializedName("tables_totales") val tablesTotales: Int?,
    val lignes: List<ReservationLine>?
)
