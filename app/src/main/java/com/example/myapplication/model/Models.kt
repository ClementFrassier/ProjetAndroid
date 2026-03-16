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

data class Editor(
    val id: Int,
    val name: String,
    val createdAt: String,
    val updatedAt: String,
    val contactsCount: Int?,
    val gamesCount: Int?,
    val reservationsCount: Int?
)

data class EditorDetail(
    val id: Int,
    val name: String,
    val createdAt: String,
    val updatedAt: String,
    val contacts: List<Contact>,
    val games: List<Game>
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
    val type: String?,
    val minAge: Int?,
    val maxAge: Int?
)

data class GameWithEditor(
    val id: Int,
    val name: String,
    val type: String?,
    val minAge: Int?,
    val maxAge: Int?,
    val editor: EditeurLight?
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
    val name: String,
    val type: String? = null,
    val minAge: Int? = null,
    val maxAge: Int? = null
)

data class GameCreateInput(
    val editorId: Int,
    val name: String,
    val type: String? = null,
    val minAge: Int? = null,
    val maxAge: Int? = null
)

data class EditorInput(
    val name: String,
    val contacts: List<ContactInput>? = null,
    val games: List<GameInput>? = null
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
    val id: Int,
    val reservationId: Int,
    val tariffZoneId: Int,
    val tablesCount: Int,
    val surfaceSquareM: Int?,
    val pricePerTableSnapshot: Double,
    val pricePerSquareMSnapshot: Double,
    val lineTotal: Double
)

data class Reservation(
    val id: Int,
    val festivalId: Int,
    val editorId: Int?,
    val reservantId: Int?,
    val totalTables: Int,
    val subtotalAmount: Double,
    val discountTables: Int,
    val discountAmount: Double,
    val finalAmount: Double,
    val workflowState: String,
    val willPresentGames: Boolean,
    val powerOutlets: Int,
    val gamesNotes: String?,
    val createdAt: String,
    val updatedAt: String,
    val editor: EditeurLight?,
    val lines: List<ReservationLine>?
    // Ignored lists for now if not needed: games, contacts, invoices
)

data class ReservationLineInput(
    val tariffZoneId: Int,
    val tablesCount: Int = 0,
    val surfaceM2: Double? = null
)

data class ReservationCreateInput(
    val festivalId: Int,
    val editorId: Int? = null,
    val reservantId: Int? = null,
    val willPresentGames: Boolean? = null,
    val powerOutlets: Int? = null,
    val discountTables: Int? = null,
    val discountAmount: Double? = null,
    val lines: List<ReservationLineInput>
)

data class ReservationUpdateInput(
    val reservantId: Int? = null,
    val editorId: Int? = null,
    val willPresentGames: Boolean? = null,
    val discountTables: Int? = null,
    val discountAmount: Double? = null,
    val powerOutlets: Int? = null,
    val gamesNotes: String? = null,
    val workflowState: String? = null
)

data class FestivalLight(
    val id: Int,
    val name: String
)

data class Invoice(
    val id: Int,
    val invoiceNumber: String,
    val issueDate: String,
    val dueDate: String,
    val amount: Double,
    val isPaid: Boolean,
    val paidAt: String? = null,
    val notes: String? = null,
    val reservationId: Int? = null,
    val festival: FestivalLight? = null,
    val editor: EditeurLight? = null,
    val reservation: Reservation? = null,
    val createdAt: String? = null
)

data class InvoiceCreateInput(
    val reservationId: Int,
    val issueDate: String,
    val dueDate: String,
    val notes: String? = null
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

// RESERVANTS
data class ReservantContactEvent(
    val id: Int,
    val statusId: Int,
    val occurredAt: String,
    val channel: String?,
    val notes: String?,
    val createdByUserId: Int?
)

data class ReservantStatus(
    val status: String,
    val lastContactAt: String?,
    val notes: String?,
    val lastContactEvent: ReservantContactEvent?
)

data class Reservant(
    val id: Int,
    val type: String,
    val displayName: String,
    val legalName: String?,
    val email: String?,
    val phone: String?,
    val notes: String?,
    val editor: EditeurLight?,
    val reservationsCount: Int? = null,
    val currentStatus: ReservantStatus? = null,
    val lastContactAt: String? = null,
    val hasReservation: Boolean? = null
)

data class CreateReservantInput(
    val type: String,
    val displayName: String,
    val legalName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val notes: String? = null,
    val editorId: Int? = null
)

data class UpdateReservantInput(
    val type: String? = null,
    val displayName: String? = null,
    val legalName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val notes: String? = null,
    val editorId: Int? = null
)

data class UpdateReservantStatusInput(
    val status: String,
    val notes: String? = null
)

data class AddReservantContactInput(
    val occurredAt: String,
    val channel: String? = null,
    val notes: String? = null
)
