package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.AppointmentEntity
import com.example.data.model.AppointmentStatus
import com.example.data.model.BillCategory
import com.example.data.model.BillEntity
import com.example.data.model.BillStatus
import com.example.data.model.DocumentEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.data.model.FamilyMemberEntity
import com.example.data.model.Frequency
import com.example.data.model.RecurringTaskEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class LifeRepository(private val database: AppDatabase) {

    val allExpenses: Flow<List<ExpenseEntity>> = database.expenseDao().getAllExpenses()
    val allBills: Flow<List<BillEntity>> = database.billDao().getAllBills()
    val allAppointments: Flow<List<AppointmentEntity>> = database.appointmentDao().getAllAppointments()
    val allTasks: Flow<List<RecurringTaskEntity>> = database.taskDao().getAllTasks()
    val allDocuments: Flow<List<DocumentEntity>> = database.documentDao().getAllDocuments()
    val allFamilyMembers: Flow<List<FamilyMemberEntity>> = database.familyDao().getAllMembers()

    // --- EXPENSES ---
    suspend fun insertExpense(expense: ExpenseEntity): Long = database.expenseDao().insertExpense(expense)
    suspend fun updateExpense(expense: ExpenseEntity) = database.expenseDao().updateExpense(expense)
    suspend fun deleteExpense(expense: ExpenseEntity) = database.expenseDao().deleteExpense(expense)
    suspend fun deleteExpenseById(id: Long) = database.expenseDao().deleteExpenseById(id)

    // --- BILLS & SMART RECURRING ENGINE ---
    suspend fun insertBill(bill: BillEntity): Long = database.billDao().insertBill(bill)
    suspend fun updateBill(bill: BillEntity) = database.billDao().updateBill(bill)
    suspend fun deleteBill(bill: BillEntity) = database.billDao().deleteBill(bill)
    suspend fun deleteBillById(id: Long) = database.billDao().deleteBillById(id)

    /**
     * Smart Recurring Engine:
     * When user taps "Mark as Paid":
     * 1. Records bill payment status as PAID
     * 2. Automatically logs an Expense entry for financial history
     * 3. Calculates the next cycle occurrence and schedules the next Bill reminder
     */
    suspend fun markBillAsPaid(
        bill: BillEntity,
        customAmount: Double? = null,
        paidDateMillis: Long = System.currentTimeMillis()
    ) {
        val amountPaid = customAmount ?: bill.amount

        // 1. Mark current bill as paid
        val updatedCurrentBill = bill.copy(
            status = BillStatus.PAID.name,
            lastPaidDateMillis = paidDateMillis
        )
        database.billDao().updateBill(updatedCurrentBill)

        // 2. Automatically log an Expense in spending history
        if (bill.autoExpenseOnPaid) {
            val mappedExpenseCategory = when (BillCategory.fromString(bill.category)) {
                BillCategory.ELECTRICITY, BillCategory.LPG_GAS, BillCategory.WATER,
                BillCategory.MOBILE_RECHARGE, BillCategory.INTERNET_OTT, BillCategory.RENT,
                BillCategory.PROPERTY_TAX -> ExpenseCategory.BILLS.name

                BillCategory.LIC_PREMIUM, BillCategory.HEALTH_INSURANCE,
                BillCategory.CAR_INSURANCE, BillCategory.BIKE_INSURANCE -> ExpenseCategory.MEDICAL.name // or BILLS

                BillCategory.LOAN_EMI -> ExpenseCategory.HOUSEHOLD.name
                else -> ExpenseCategory.BILLS.name
            }

            val expense = ExpenseEntity(
                title = "Paid: ${bill.name}",
                amount = amountPaid,
                category = mappedExpenseCategory,
                dateMillis = paidDateMillis,
                notes = "Auto-recorded from bill payment. ${bill.notes}",
                familyMember = bill.familyMember,
                paymentMethod = "Manual Paid Record",
                linkedBillId = bill.id
            )
            database.expenseDao().insertExpense(expense)
        }

        // 3. If recurring, schedule the next occurrence!
        val freq = Frequency.fromString(bill.frequency)
        if (freq != Frequency.ONE_TIME) {
            val nextDueDate = calculateNextOccurrence(bill.dueDateMillis, freq)
            val nextBill = BillEntity(
                name = bill.name,
                amount = bill.amount,
                dueDateMillis = nextDueDate,
                frequency = bill.frequency,
                category = bill.category,
                reminderDaysBefore = bill.reminderDaysBefore,
                status = BillStatus.PENDING.name,
                notes = bill.notes,
                docPolicyNo = bill.docPolicyNo,
                renewalDateMillis = if (bill.renewalDateMillis != null) calculateNextOccurrence(bill.renewalDateMillis, freq) else null,
                familyMember = bill.familyMember,
                autoExpenseOnPaid = bill.autoExpenseOnPaid
            )
            database.billDao().insertBill(nextBill)
        }
    }

    suspend fun snoozeBill(bill: BillEntity, daysToAdd: Int) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = bill.dueDateMillis
            add(Calendar.DAY_OF_YEAR, daysToAdd)
        }
        val updated = bill.copy(
            dueDateMillis = cal.timeInMillis,
            status = BillStatus.PENDING.name
        )
        database.billDao().updateBill(updated)
    }

    private fun calculateNextOccurrence(baseMillis: Long, frequency: Frequency): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = baseMillis
        }
        when (frequency) {
            Frequency.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
            Frequency.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            Frequency.MONTHLY -> cal.add(Calendar.MONTH, 1)
            Frequency.QUARTERLY -> cal.add(Calendar.MONTH, 3)
            Frequency.HALF_YEARLY -> cal.add(Calendar.MONTH, 6)
            Frequency.YEARLY -> cal.add(Calendar.YEAR, 1)
            Frequency.ONE_TIME -> {}
        }
        return cal.timeInMillis
    }

    // --- APPOINTMENTS ---
    suspend fun insertAppointment(appointment: AppointmentEntity): Long = database.appointmentDao().insertAppointment(appointment)
    suspend fun updateAppointment(appointment: AppointmentEntity) = database.appointmentDao().updateAppointment(appointment)
    suspend fun deleteAppointment(appointment: AppointmentEntity) = database.appointmentDao().deleteAppointment(appointment)
    suspend fun deleteAppointmentById(id: Long) = database.appointmentDao().deleteAppointmentById(id)

    suspend fun updateAppointmentStatus(appointment: AppointmentEntity, status: AppointmentStatus) {
        database.appointmentDao().updateAppointment(appointment.copy(status = status.name))
    }

    suspend fun rescheduleAppointment(appointment: AppointmentEntity, newDateTimeMillis: Long) {
        database.appointmentDao().updateAppointment(
            appointment.copy(
                dateTimeMillis = newDateTimeMillis,
                status = AppointmentStatus.UPCOMING.name
            )
        )
    }

    // --- RECURRING TASKS ---
    suspend fun insertTask(task: RecurringTaskEntity): Long = database.taskDao().insertTask(task)
    suspend fun updateTask(task: RecurringTaskEntity) = database.taskDao().updateTask(task)
    suspend fun deleteTask(task: RecurringTaskEntity) = database.taskDao().deleteTask(task)
    suspend fun deleteTaskById(id: Long) = database.taskDao().deleteTaskById(id)

    suspend fun toggleTask(task: RecurringTaskEntity) {
        val isNowCompleted = !task.isCompletedToday
        val newStreak = if (isNowCompleted) task.streakCount + 1 else (task.streakCount - 1).coerceAtLeast(0)
        val updated = task.copy(
            isCompletedToday = isNowCompleted,
            lastCompletedDateMillis = if (isNowCompleted) System.currentTimeMillis() else task.lastCompletedDateMillis,
            streakCount = newStreak
        )
        database.taskDao().updateTask(updated)
    }

    suspend fun resetAllDailyTasks() {
        // Reset tasks for a new day
        val tasks = database.taskDao().getAllTasks()
        // could be handled periodically or on app launch
    }

    // --- DOCUMENTS ---
    suspend fun insertDocument(doc: DocumentEntity): Long = database.documentDao().insertDocument(doc)
    suspend fun updateDocument(doc: DocumentEntity) = database.documentDao().updateDocument(doc)
    suspend fun deleteDocument(doc: DocumentEntity) = database.documentDao().deleteDocument(doc)
    suspend fun deleteDocumentById(id: Long) = database.documentDao().deleteDocumentById(id)

    // --- FAMILY MEMBERS ---
    suspend fun insertFamilyMember(member: FamilyMemberEntity): Long = database.familyDao().insertMember(member)
    suspend fun deleteFamilyMember(member: FamilyMemberEntity) = database.familyDao().deleteMember(member)

    // --- RESET / DEMO DATA ---
    suspend fun reloadDemoData() {
        AppDatabase.populateInitialData(database)
    }
}
