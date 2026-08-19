package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AppointmentCategory
import com.example.data.model.AppointmentEntity
import com.example.data.model.AppointmentStatus
import com.example.data.model.BillCategory
import com.example.data.model.BillEntity
import com.example.data.model.BillStatus
import com.example.data.model.DocType
import com.example.data.model.DocumentEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.data.model.FamilyMemberEntity
import com.example.data.model.FamilyRelation
import com.example.data.model.Frequency
import com.example.data.model.RecurringTaskEntity
import com.example.data.model.TaskFrequency
import com.example.data.repository.LifeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppTab(val title: String, val iconName: String) {
    TODAY("Today", "Home"),
    BILLS("Bills", "Receipt"),
    EXPENSES("Expenses", "AccountBalanceWallet"),
    APPOINTMENTS("Appointments", "CalendarMonth"),
    TASKS("Tasks", "CheckCircle"),
    DOCUMENTS("Documents", "Folder"),
    CALENDAR("Calendar", "DateRange"),
    REPORTS("Reports", "Insights")
}

data class LifeUiState(
    val selectedTab: AppTab = AppTab.TODAY,
    val selectedFamilyFilter: String = "ALL", // "ALL" or specific member name
    val searchQuery: String = "",
    val currencySymbol: String = "₹",
    val selectedCalendarDateMillis: Long = System.currentTimeMillis(),
    val expensePeriodFilter: String = "Monthly", // Daily, Weekly, Monthly, Quarterly, Yearly
    val billFilterStatus: String = "ALL", // ALL, PENDING, OVERDUE, PAID, RECURRING
    val appointmentCategoryFilter: String = "ALL",
    val taskFrequencyFilter: String = "ALL",
    val activeBillToPay: BillEntity? = null,
    val activeAppointmentToEdit: AppointmentEntity? = null,
    val activeDocumentToEdit: DocumentEntity? = null,
    val showAddExpenseDialog: Boolean = false,
    val showAddBillDialog: Boolean = false,
    val showAddAppointmentDialog: Boolean = false,
    val showAddTaskDialog: Boolean = false,
    val showAddDocumentDialog: Boolean = false,
    val showAddFamilyMemberDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showExportDialog: Boolean = false,
    val showQuickActionSheet: Boolean = false,
    val successToastMessage: String? = null
)

class LifeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LifeRepository = LifeRepository(
        AppDatabase.getDatabase(application, viewModelScope)
    )

    private val _uiState = MutableStateFlow(LifeUiState())
    val uiState: StateFlow<LifeUiState> = _uiState.asStateFlow()

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBills: StateFlow<List<BillEntity>> = repository.allBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAppointments: StateFlow<List<AppointmentEntity>> = repository.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<RecurringTaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDocuments: StateFlow<List<DocumentEntity>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFamilyMembers: StateFlow<List<FamilyMemberEntity>> = repository.allFamilyMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- NAVIGATION & UI ACTIONS ---
    fun selectTab(tab: AppTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun setFamilyFilter(member: String) {
        _uiState.value = _uiState.value.copy(selectedFamilyFilter = member)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setExpensePeriodFilter(period: String) {
        _uiState.value = _uiState.value.copy(expensePeriodFilter = period)
    }

    fun setBillFilterStatus(status: String) {
        _uiState.value = _uiState.value.copy(billFilterStatus = status)
    }

    fun setAppointmentCategoryFilter(cat: String) {
        _uiState.value = _uiState.value.copy(appointmentCategoryFilter = cat)
    }

    fun setTaskFrequencyFilter(freq: String) {
        _uiState.value = _uiState.value.copy(taskFrequencyFilter = freq)
    }

    fun selectCalendarDate(millis: Long) {
        _uiState.value = _uiState.value.copy(selectedCalendarDateMillis = millis)
    }

    fun setCurrencySymbol(symbol: String) {
        _uiState.value = _uiState.value.copy(currencySymbol = symbol)
    }

    // Dialog toggles
    fun setShowAddExpense(show: Boolean) { _uiState.value = _uiState.value.copy(showAddExpenseDialog = show) }
    fun setShowAddBill(show: Boolean) { _uiState.value = _uiState.value.copy(showAddBillDialog = show) }
    fun setShowAddAppointment(show: Boolean) { _uiState.value = _uiState.value.copy(showAddAppointmentDialog = show) }
    fun setShowAddTask(show: Boolean) { _uiState.value = _uiState.value.copy(showAddTaskDialog = show) }
    fun setShowAddDocument(show: Boolean) { _uiState.value = _uiState.value.copy(showAddDocumentDialog = show) }
    fun setShowAddFamilyMember(show: Boolean) { _uiState.value = _uiState.value.copy(showAddFamilyMemberDialog = show) }
    fun setShowSettings(show: Boolean) { _uiState.value = _uiState.value.copy(showSettingsDialog = show) }
    fun setShowExport(show: Boolean) { _uiState.value = _uiState.value.copy(showExportDialog = show) }
    fun setShowQuickActionSheet(show: Boolean) { _uiState.value = _uiState.value.copy(showQuickActionSheet = show) }

    fun openPayBillDialog(bill: BillEntity) {
        _uiState.value = _uiState.value.copy(activeBillToPay = bill)
    }

    fun closePayBillDialog() {
        _uiState.value = _uiState.value.copy(activeBillToPay = null)
    }

    fun clearToastMessage() {
        _uiState.value = _uiState.value.copy(successToastMessage = null)
    }

    // --- SMART RECURRING & BILL ACTIONS ---
    fun markBillAsPaid(bill: BillEntity, customAmount: Double? = null, paidDate: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            repository.markBillAsPaid(bill, customAmount, paidDate)
            val freqText = if (Frequency.fromString(bill.frequency) != Frequency.ONE_TIME) " & Next ${bill.frequency} reminder scheduled!" else ""
            _uiState.value = _uiState.value.copy(
                activeBillToPay = null,
                successToastMessage = "✓ Marked '${bill.name}' as paid. Recorded in expenses$freqText"
            )
        }
    }

    fun snoozeBill(bill: BillEntity, days: Int = 1) {
        viewModelScope.launch {
            repository.snoozeBill(bill, days)
            _uiState.value = _uiState.value.copy(
                successToastMessage = "⏰ Snoozed '${bill.name}' by $days day(s)"
            )
        }
    }

    fun addBill(
        name: String,
        amount: Double,
        dueDateMillis: Long,
        frequency: String,
        category: String,
        reminderDaysBefore: Int,
        notes: String,
        docPolicyNo: String,
        familyMember: String,
        autoExpenseOnPaid: Boolean
    ) {
        viewModelScope.launch {
            val bill = BillEntity(
                name = name,
                amount = amount,
                dueDateMillis = dueDateMillis,
                frequency = frequency,
                category = category,
                reminderDaysBefore = reminderDaysBefore,
                status = BillStatus.PENDING.name,
                notes = notes,
                docPolicyNo = docPolicyNo,
                renewalDateMillis = if (frequency == Frequency.YEARLY.name) dueDateMillis else null,
                familyMember = familyMember,
                autoExpenseOnPaid = autoExpenseOnPaid
            )
            repository.insertBill(bill)
            _uiState.value = _uiState.value.copy(
                showAddBillDialog = false,
                successToastMessage = "✓ Added bill reminder: $name"
            )
        }
    }

    fun deleteBill(bill: BillEntity) {
        viewModelScope.launch {
            repository.deleteBill(bill)
            _uiState.value = _uiState.value.copy(
                successToastMessage = "Deleted bill: ${bill.name}"
            )
        }
    }

    // --- EXPENSES ---
    fun addExpense(
        title: String,
        amount: Double,
        category: String,
        dateMillis: Long,
        notes: String,
        familyMember: String,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            val expense = ExpenseEntity(
                title = title,
                amount = amount,
                category = category,
                dateMillis = dateMillis,
                notes = notes,
                familyMember = familyMember,
                paymentMethod = paymentMethod
            )
            repository.insertExpense(expense)
            _uiState.value = _uiState.value.copy(
                showAddExpenseDialog = false,
                successToastMessage = "✓ Recorded expense: $title (${_uiState.value.currencySymbol}$amount)"
            )
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _uiState.value = _uiState.value.copy(
                successToastMessage = "Deleted expense: ${expense.title}"
            )
        }
    }

    // --- APPOINTMENTS ---
    fun addAppointment(
        title: String,
        personOrLocation: String,
        category: String,
        dateTimeMillis: Long,
        reminderHoursBefore: Int,
        reminderMinutesBefore: Int,
        familyMember: String,
        notes: String
    ) {
        viewModelScope.launch {
            val appt = AppointmentEntity(
                title = title,
                personOrLocation = personOrLocation,
                category = category,
                dateTimeMillis = dateTimeMillis,
                reminderHoursBefore = reminderHoursBefore,
                reminderMinutesBefore = reminderMinutesBefore,
                familyMember = familyMember,
                status = AppointmentStatus.UPCOMING.name,
                notes = notes
            )
            repository.insertAppointment(appt)
            _uiState.value = _uiState.value.copy(
                showAddAppointmentDialog = false,
                successToastMessage = "✓ Added appointment: $title"
            )
        }
    }

    fun updateAppointmentStatus(appointment: AppointmentEntity, status: AppointmentStatus) {
        viewModelScope.launch {
            repository.updateAppointmentStatus(appointment, status)
            _uiState.value = _uiState.value.copy(
                successToastMessage = "Appointment marked as ${status.displayName}"
            )
        }
    }

    fun deleteAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            repository.deleteAppointment(appointment)
            _uiState.value = _uiState.value.copy(
                successToastMessage = "Deleted appointment: ${appointment.title}"
            )
        }
    }

    // --- RECURRING TASKS ---
    fun toggleTask(task: RecurringTaskEntity) {
        viewModelScope.launch {
            repository.toggleTask(task)
        }
    }

    fun addTask(
        title: String,
        frequency: String,
        category: String,
        familyMember: String,
        notes: String
    ) {
        viewModelScope.launch {
            val task = RecurringTaskEntity(
                title = title,
                frequency = frequency,
                category = category,
                familyMember = familyMember,
                notes = notes,
                streakCount = 0,
                isCompletedToday = false
            )
            repository.insertTask(task)
            _uiState.value = _uiState.value.copy(
                showAddTaskDialog = false,
                successToastMessage = "✓ Added recurring task: $title"
            )
        }
    }

    fun deleteTask(task: RecurringTaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            _uiState.value = _uiState.value.copy(
                successToastMessage = "Deleted task: ${task.title}"
            )
        }
    }

    // --- DOCUMENTS ---
    fun addDocument(
        title: String,
        docType: String,
        identifierOrPolicyNo: String,
        issuerOrProvider: String,
        expiryDateMillis: Long?,
        notes: String,
        familyMember: String,
        linkedBillName: String
    ) {
        viewModelScope.launch {
            val doc = DocumentEntity(
                title = title,
                docType = docType,
                identifierOrPolicyNo = identifierOrPolicyNo,
                issuerOrProvider = issuerOrProvider,
                expiryDateMillis = expiryDateMillis,
                notes = notes,
                familyMember = familyMember,
                linkedBillName = linkedBillName
            )
            repository.insertDocument(doc)
            _uiState.value = _uiState.value.copy(
                showAddDocumentDialog = false,
                successToastMessage = "✓ Added document: $title"
            )
        }
    }

    fun deleteDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            repository.deleteDocument(doc)
            _uiState.value = _uiState.value.copy(
                successToastMessage = "Deleted document: ${doc.title}"
            )
        }
    }

    // --- FAMILY MEMBERS ---
    fun addFamilyMember(name: String, relation: String, emoji: String, colorHex: String) {
        viewModelScope.launch {
            val member = FamilyMemberEntity(
                name = name,
                relation = relation,
                emoji = emoji,
                colorHex = colorHex
            )
            repository.insertFamilyMember(member)
            _uiState.value = _uiState.value.copy(
                showAddFamilyMemberDialog = false,
                successToastMessage = "✓ Added family member: $name"
            )
        }
    }

    fun deleteFamilyMember(member: FamilyMemberEntity) {
        viewModelScope.launch {
            repository.deleteFamilyMember(member)
            _uiState.value = _uiState.value.copy(
                successToastMessage = "Removed family member: ${member.name}"
            )
        }
    }

    fun reloadDemoData() {
        viewModelScope.launch {
            repository.reloadDemoData()
            _uiState.value = _uiState.value.copy(
                showSettingsDialog = false,
                successToastMessage = "✓ Sample data reloaded successfully"
            )
        }
    }
}
