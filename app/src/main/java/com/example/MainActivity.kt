package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.LifeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LifeRemindApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeRemindApp(
    viewModel: LifeViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bills by viewModel.allBills.collectAsStateWithLifecycle()
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val appointments by viewModel.allAppointments.collectAsStateWithLifecycle()
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val documents by viewModel.allDocuments.collectAsStateWithLifecycle()
    val familyMembers by viewModel.allFamilyMembers.collectAsStateWithLifecycle()

    // Show toast messages when action finishes
    LaunchedEffect(uiState.successToastMessage) {
        uiState.successToastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    Scaffold(
        topBar = {
            LifeTopAppBar(
                title = when (uiState.selectedTab) {
                    AppTab.TODAY -> "LifeRemind"
                    AppTab.BILLS -> "Bills & Payments"
                    AppTab.EXPENSES -> "Expense Spending"
                    AppTab.APPOINTMENTS -> "Appointments"
                    AppTab.TASKS -> "Recurring Tasks"
                    AppTab.DOCUMENTS -> "Document Vault"
                    AppTab.CALENDAR -> "Life Calendar"
                    AppTab.REPORTS -> "Financial Reports"
                },
                subtitle = when (uiState.selectedTab) {
                    AppTab.TODAY -> "Personal Life & Payment Reminder Hub"
                    AppTab.BILLS -> "Recurring & One-time Due Reminders"
                    AppTab.EXPENSES -> "Manual Spending Tracker"
                    AppTab.APPOINTMENTS -> "Hospital, School & Meetings"
                    AppTab.TASKS -> "Daily & Yearly Habits"
                    AppTab.DOCUMENTS -> "Insurance & Policy Records"
                    AppTab.CALENDAR -> "Due Dates & Events by Day"
                    AppTab.REPORTS -> "Spending & Liability Breakdown"
                },
                onSettingsClick = { viewModel.setShowSettings(true) },
                modifier = Modifier.testTag("app_top_bar")
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bottom_nav_bar"),
                windowInsets = WindowInsets.navigationBars
            ) {
                val navItems = listOf(
                    Triple(AppTab.TODAY, Icons.Default.Home, "Today"),
                    Triple(AppTab.BILLS, Icons.Default.Notifications, "Bills"),
                    Triple(AppTab.EXPENSES, Icons.Default.AccountBalanceWallet, "Expenses"),
                    Triple(AppTab.APPOINTMENTS, Icons.Default.Event, "Meetings"),
                    Triple(AppTab.TASKS, Icons.Default.CheckCircle, "Tasks"),
                    Triple(AppTab.CALENDAR, Icons.Default.DateRange, "Calendar"),
                    Triple(AppTab.DOCUMENTS, Icons.Default.Folder, "Vault"),
                    Triple(AppTab.REPORTS, Icons.Default.Insights, "Reports")
                )

                // Render primary tabs
                navItems.take(5).forEach { (tab, icon, label) ->
                    val selected = uiState.selectedTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.selectTab(tab) },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 10.sp, maxLines = 1) },
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }

                // Render More dropdown/popup for Calendar, Documents, Reports
                val isSecondarySelected = uiState.selectedTab in listOf(AppTab.CALENDAR, AppTab.DOCUMENTS, AppTab.REPORTS)
                var moreMenuExpanded by remember { mutableStateOf(false) }

                NavigationBarItem(
                    selected = isSecondarySelected,
                    onClick = { moreMenuExpanded = true },
                    icon = {
                        Box {
                            Icon(Icons.Default.MoreHoriz, contentDescription = "More")
                            DropdownMenu(
                                expanded = moreMenuExpanded,
                                onDismissRequest = { moreMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("🗓️ Calendar Agenda") },
                                    onClick = {
                                        viewModel.selectTab(AppTab.CALENDAR)
                                        moreMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("📁 Document & Policy Vault") },
                                    onClick = {
                                        viewModel.selectTab(AppTab.DOCUMENTS)
                                        moreMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("📊 Spending Reports & Overview") },
                                    onClick = {
                                        viewModel.selectTab(AppTab.REPORTS)
                                        moreMenuExpanded = false
                                    }
                                )
                            }
                        }
                    },
                    label = { Text("More", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_tab_more")
                )
            }
        },
        floatingActionButton = {
            if (uiState.selectedTab == AppTab.TODAY) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.setShowQuickActionSheet(true) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.Add, contentDescription = "Quick Add") },
                    text = { Text("Quick Add", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.padding(bottom = 60.dp).testTag("today_quick_add_fab")
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.selectedTab) {
                AppTab.TODAY -> TodayScreen(
                    bills = bills,
                    expenses = expenses,
                    appointments = appointments,
                    tasks = tasks,
                    familyMembers = familyMembers,
                    selectedFamily = uiState.selectedFamilyFilter,
                    currencySymbol = uiState.currencySymbol,
                    onFamilySelect = { viewModel.setFamilyFilter(it) },
                    onPayBillClick = { viewModel.openPayBillDialog(it) },
                    onSnoozeBillClick = { viewModel.snoozeBill(it, 1) },
                    onToggleTask = { viewModel.toggleTask(it) },
                    onAddExpenseClick = { viewModel.setShowAddExpense(true) },
                    onAddBillClick = { viewModel.setShowAddBill(true) },
                    onAddAppointmentClick = { viewModel.setShowAddAppointment(true) },
                    onViewAllBills = { viewModel.selectTab(AppTab.BILLS) },
                    onViewAllExpenses = { viewModel.selectTab(AppTab.EXPENSES) },
                    onViewAllAppointments = { viewModel.selectTab(AppTab.APPOINTMENTS) },
                    onViewAllTasks = { viewModel.selectTab(AppTab.TASKS) }
                )
                AppTab.BILLS -> BillsScreen(
                    bills = bills,
                    familyMembers = familyMembers,
                    selectedFamily = uiState.selectedFamilyFilter,
                    filterStatus = uiState.billFilterStatus,
                    currencySymbol = uiState.currencySymbol,
                    onFamilySelect = { viewModel.setFamilyFilter(it) },
                    onFilterStatusSelect = { viewModel.setBillFilterStatus(it) },
                    onPayBillClick = { viewModel.openPayBillDialog(it) },
                    onSnoozeBillClick = { viewModel.snoozeBill(it, 1) },
                    onDeleteBill = { viewModel.deleteBill(it) },
                    onAddBillClick = { viewModel.setShowAddBill(true) }
                )
                AppTab.EXPENSES -> ExpensesScreen(
                    expenses = expenses,
                    familyMembers = familyMembers,
                    selectedFamily = uiState.selectedFamilyFilter,
                    periodFilter = uiState.expensePeriodFilter,
                    currencySymbol = uiState.currencySymbol,
                    onFamilySelect = { viewModel.setFamilyFilter(it) },
                    onPeriodSelect = { viewModel.setExpensePeriodFilter(it) },
                    onDeleteExpense = { viewModel.deleteExpense(it) },
                    onAddExpenseClick = { viewModel.setShowAddExpense(true) }
                )
                AppTab.APPOINTMENTS -> AppointmentsScreen(
                    appointments = appointments,
                    familyMembers = familyMembers,
                    selectedFamily = uiState.selectedFamilyFilter,
                    selectedCategory = uiState.appointmentCategoryFilter,
                    onFamilySelect = { viewModel.setFamilyFilter(it) },
                    onCategorySelect = { viewModel.setAppointmentCategoryFilter(it) },
                    onStatusChange = { appt, status -> viewModel.updateAppointmentStatus(appt, status) },
                    onDeleteAppointment = { viewModel.deleteAppointment(it) },
                    onAddAppointmentClick = { viewModel.setShowAddAppointment(true) }
                )
                AppTab.TASKS -> TasksScreen(
                    tasks = tasks,
                    familyMembers = familyMembers,
                    selectedFamily = uiState.selectedFamilyFilter,
                    selectedFrequency = uiState.taskFrequencyFilter,
                    onFamilySelect = { viewModel.setFamilyFilter(it) },
                    onFrequencySelect = { viewModel.setTaskFrequencyFilter(it) },
                    onToggleTask = { viewModel.toggleTask(it) },
                    onDeleteTask = { viewModel.deleteTask(it) },
                    onAddTaskClick = { viewModel.setShowAddTask(true) }
                )
                AppTab.DOCUMENTS -> DocumentsScreen(
                    documents = documents,
                    familyMembers = familyMembers,
                    selectedFamily = uiState.selectedFamilyFilter,
                    onFamilySelect = { viewModel.setFamilyFilter(it) },
                    onDeleteDocument = { viewModel.deleteDocument(it) },
                    onAddDocumentClick = { viewModel.setShowAddDocument(true) }
                )
                AppTab.CALENDAR -> CalendarScreen(
                    bills = bills,
                    expenses = expenses,
                    appointments = appointments,
                    tasks = tasks,
                    selectedDateMillis = uiState.selectedCalendarDateMillis,
                    currencySymbol = uiState.currencySymbol,
                    onSelectDate = { viewModel.selectCalendarDate(it) },
                    onPayBillClick = { viewModel.openPayBillDialog(it) }
                )
                AppTab.REPORTS -> ReportsScreen(
                    expenses = expenses,
                    bills = bills,
                    currencySymbol = uiState.currencySymbol
                )
            }
        }
    }

    // --- DIALOG MODALS ---

    if (uiState.showAddExpenseDialog) {
        AddExpenseDialog(
            familyMembers = familyMembers,
            currencySymbol = uiState.currencySymbol,
            onDismiss = { viewModel.setShowAddExpense(false) },
            onConfirm = { title, amount, category, dateMillis, notes, familyMember, paymentMethod ->
                viewModel.addExpense(title, amount, category, dateMillis, notes, familyMember, paymentMethod)
            }
        )
    }

    if (uiState.showAddBillDialog) {
        AddBillDialog(
            familyMembers = familyMembers,
            currencySymbol = uiState.currencySymbol,
            onDismiss = { viewModel.setShowAddBill(false) },
            onConfirm = { name, amount, dueDateMillis, frequency, category, reminderDaysBefore, notes, docPolicyNo, familyMember, autoExpenseOnPaid ->
                viewModel.addBill(name, amount, dueDateMillis, frequency, category, reminderDaysBefore, notes, docPolicyNo, familyMember, autoExpenseOnPaid)
            }
        )
    }

    if (uiState.activeBillToPay != null) {
        PayBillConfirmDialog(
            bill = uiState.activeBillToPay!!,
            currencySymbol = uiState.currencySymbol,
            onDismiss = { viewModel.closePayBillDialog() },
            onConfirmPay = { bill, customAmount, paidDate ->
                viewModel.markBillAsPaid(bill, customAmount, paidDate)
            }
        )
    }

    if (uiState.showAddAppointmentDialog) {
        AddAppointmentDialog(
            familyMembers = familyMembers,
            onDismiss = { viewModel.setShowAddAppointment(false) },
            onConfirm = { title, personOrLocation, category, dateTimeMillis, reminderHoursBefore, reminderMinutesBefore, familyMember, notes ->
                viewModel.addAppointment(title, personOrLocation, category, dateTimeMillis, reminderHoursBefore, reminderMinutesBefore, familyMember, notes)
            }
        )
    }

    if (uiState.showAddTaskDialog) {
        AddTaskDialog(
            familyMembers = familyMembers,
            onDismiss = { viewModel.setShowAddTask(false) },
            onConfirm = { title, frequency, category, familyMember, notes ->
                viewModel.addTask(title, frequency, category, familyMember, notes)
            }
        )
    }

    if (uiState.showAddDocumentDialog) {
        AddDocumentDialog(
            familyMembers = familyMembers,
            onDismiss = { viewModel.setShowAddDocument(false) },
            onConfirm = { title, docType, identifierOrPolicyNo, issuerOrProvider, expiryDateMillis, notes, familyMember, linkedBillName ->
                viewModel.addDocument(title, docType, identifierOrPolicyNo, issuerOrProvider, expiryDateMillis, notes, familyMember, linkedBillName)
            }
        )
    }

    if (uiState.showAddFamilyMemberDialog) {
        AddFamilyMemberDialog(
            onDismiss = { viewModel.setShowAddFamilyMember(false) },
            onConfirm = { name, relation, emoji, colorHex ->
                viewModel.addFamilyMember(name, relation, emoji, colorHex)
            }
        )
    }

    if (uiState.showSettingsDialog) {
        SettingsDialog(
            currentCurrency = uiState.currencySymbol,
            onSelectCurrency = { viewModel.setCurrencySymbol(it) },
            onReloadDemoData = { viewModel.reloadDemoData() },
            onAddFamilyMemberClick = { viewModel.setShowAddFamilyMember(true) },
            onDismiss = { viewModel.setShowSettings(false) }
        )
    }

    if (uiState.showQuickActionSheet) {
        QuickActionSheet(
            onAddExpense = { viewModel.setShowAddExpense(true) },
            onAddBill = { viewModel.setShowAddBill(true) },
            onAddAppointment = { viewModel.setShowAddAppointment(true) },
            onAddTask = { viewModel.setShowAddTask(true) },
            onAddDocument = { viewModel.setShowAddDocument(true) },
            onDismiss = { viewModel.setShowQuickActionSheet(false) }
        )
    }
}
