package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String, // from ExpenseCategory
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = "",
    val familyMember: String = "Self",
    val paymentMethod: String = "Cash/UPI",
    val linkedBillId: Long? = null
)

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    val dueDateMillis: Long,
    val frequency: String = Frequency.MONTHLY.name,
    val category: String = BillCategory.ELECTRICITY.name,
    val reminderDaysBefore: Int = 3,
    val status: String = BillStatus.PENDING.name,
    val notes: String = "",
    val docPolicyNo: String = "",
    val renewalDateMillis: Long? = null,
    val familyMember: String = "Family",
    val autoExpenseOnPaid: Boolean = true,
    val lastPaidDateMillis: Long? = null
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val personOrLocation: String, // e.g. "Dr. Kumar", "City High School"
    val category: String = AppointmentCategory.HOSPITAL.name,
    val dateTimeMillis: Long,
    val reminderHoursBefore: Int = 24, // e.g. 1 day before
    val reminderMinutesBefore: Int = 120, // e.g. 2 hours before
    val familyMember: String = "Self",
    val status: String = AppointmentStatus.UPCOMING.name,
    val notes: String = ""
)

@Entity(tableName = "recurring_tasks")
data class RecurringTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val frequency: String = TaskFrequency.DAILY.name,
    val category: String = "Chore",
    val familyMember: String = "Self",
    val lastCompletedDateMillis: Long? = null,
    val streakCount: Int = 0,
    val notes: String = "",
    val isCompletedToday: Boolean = false
)

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val docType: String = DocType.HEALTH_INSURANCE.name,
    val identifierOrPolicyNo: String = "",
    val issuerOrProvider: String = "", // e.g. "Star Health", "HDFC ERGO", "TNEB"
    val expiryDateMillis: Long? = null,
    val notes: String = "",
    val familyMember: String = "Self",
    val linkedBillName: String = "",
    val documentUriOrTag: String = ""
)

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val relation: String = FamilyRelation.SELF.name,
    val emoji: String = "👤",
    val colorHex: String = "#1D4ED8"
)
