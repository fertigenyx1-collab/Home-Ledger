package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.util.DateTimeUtils

@Composable
fun TodayScreen(
    bills: List<BillEntity>,
    expenses: List<ExpenseEntity>,
    appointments: List<AppointmentEntity>,
    tasks: List<RecurringTaskEntity>,
    familyMembers: List<FamilyMemberEntity>,
    selectedFamily: String,
    currencySymbol: String,
    onFamilySelect: (String) -> Unit,
    onPayBillClick: (BillEntity) -> Unit,
    onSnoozeBillClick: (BillEntity) -> Unit,
    onToggleTask: (RecurringTaskEntity) -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddBillClick: () -> Unit,
    onAddAppointmentClick: () -> Unit,
    onViewAllBills: () -> Unit,
    onViewAllExpenses: () -> Unit,
    onViewAllAppointments: () -> Unit,
    onViewAllTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter items by family member if selected
    val filteredBills = if (selectedFamily == "ALL") bills else bills.filter { it.familyMember.equals(selectedFamily, ignoreCase = true) || it.familyMember == "Family" }
    val filteredExpenses = if (selectedFamily == "ALL") expenses else expenses.filter { it.familyMember.equals(selectedFamily, ignoreCase = true) || it.familyMember == "Family" }
    val filteredAppointments = if (selectedFamily == "ALL") appointments else appointments.filter { it.familyMember.equals(selectedFamily, ignoreCase = true) }
    val filteredTasks = if (selectedFamily == "ALL") tasks else tasks.filter { it.familyMember.equals(selectedFamily, ignoreCase = true) || it.familyMember == "Family" }

    // Today & Overdue Bills (Pending)
    val dueTodayOrOverdueBills = filteredBills.filter { bill ->
        bill.status == BillStatus.PENDING.name && (DateTimeUtils.isToday(bill.dueDateMillis) || DateTimeUtils.isOverdue(bill.dueDateMillis))
    }

    // Upcoming Bills (Next 1-14 days)
    val upcomingBills = filteredBills.filter { bill ->
        bill.status == BillStatus.PENDING.name && !DateTimeUtils.isToday(bill.dueDateMillis) && !DateTimeUtils.isOverdue(bill.dueDateMillis) && DateTimeUtils.isWithinNextDays(bill.dueDateMillis, 14)
    }

    // Today's Expenses
    val todayExpenses = filteredExpenses.filter { DateTimeUtils.isToday(it.dateMillis) }
    val todayExpenseTotal = todayExpenses.sumOf { it.amount }

    // Upcoming Appointments (This Week)
    val thisWeekAppointments = filteredAppointments.filter {
        it.status == AppointmentStatus.UPCOMING.name && DateTimeUtils.isWithinNextDays(it.dateTimeMillis, 7)
    }

    // Daily Recurring Tasks
    val dailyTasks = filteredTasks.filter { it.frequency == TaskFrequency.DAILY.name }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("today_screen_list"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Family Filter Bar
        item {
            FamilyFilterRow(
                members = familyMembers,
                selectedMember = selectedFamily,
                onSelectMember = onFamilySelect
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Top Greeting & Summary Card
        item {
            TodayHeroCard(
                dueTodayCount = dueTodayOrOverdueBills.size,
                todayExpenseTotal = todayExpenseTotal,
                nextAppointment = thisWeekAppointments.firstOrNull(),
                currencySymbol = currencySymbol,
                onAddExpenseClick = onAddExpenseClick,
                onAddBillClick = onAddBillClick
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 🔴 SECTION 1: DUE TODAY & OVERDUE BILLS
        if (dueTodayOrOverdueBills.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "🔴 Due Today & Overdue",
                    badgeCount = dueTodayOrOverdueBills.size,
                    actionText = "View All Bills",
                    onActionClick = onViewAllBills
                )
            }
            items(dueTodayOrOverdueBills, key = { "due_bill_${it.id}" }) { bill ->
                TodayDueBillCard(
                    bill = bill,
                    currencySymbol = currencySymbol,
                    onPayClick = { onPayBillClick(bill) },
                    onSnoozeClick = { onSnoozeBillClick(bill) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                )
            }
            item { Spacer(modifier = Modifier.height(14.dp)) }
        }

        // 💰 SECTION 2: TODAY'S EXPENSES
        item {
            SectionHeader(
                title = "💰 Today's Expenses",
                actionText = "+ Quick Add",
                onActionClick = onAddExpenseClick
            )
            TodayExpenseSummaryCard(
                totalAmount = todayExpenseTotal,
                expenses = todayExpenses,
                currencySymbol = currencySymbol,
                onAddClick = onAddExpenseClick,
                onViewHistoryClick = onViewAllExpenses,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 🟡 SECTION 3: UPCOMING PAYMENTS
        if (upcomingBills.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "🟡 Upcoming Payments",
                    badgeCount = upcomingBills.size,
                    actionText = "All Bills",
                    onActionClick = onViewAllBills
                )
            }
            items(upcomingBills.take(4), key = { "upcoming_bill_${it.id}" }) { bill ->
                UpcomingBillItemRow(
                    bill = bill,
                    currencySymbol = currencySymbol,
                    onPayClick = { onPayBillClick(bill) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            item { Spacer(modifier = Modifier.height(14.dp)) }
        }

        // 📅 SECTION 4: THIS WEEK'S APPOINTMENTS & MEETINGS
        if (thisWeekAppointments.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "📅 This Week's Appointments",
                    badgeCount = thisWeekAppointments.size,
                    actionText = "All Appointments",
                    onActionClick = onViewAllAppointments
                )
            }
            items(thisWeekAppointments.take(3), key = { "appt_${it.id}" }) { appt ->
                TodayAppointmentCard(
                    appointment = appt,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            item { Spacer(modifier = Modifier.height(14.dp)) }
        }

        // 🔄 SECTION 5: TODAY'S RECURRING TASKS
        if (dailyTasks.isNotEmpty()) {
            item {
                val completedCount = dailyTasks.count { it.isCompletedToday }
                SectionHeader(
                    title = "🔄 Today's Habits & Tasks ($completedCount/${dailyTasks.size})",
                    actionText = "All Tasks",
                    onActionClick = onViewAllTasks
                )
            }
            items(dailyTasks, key = { "task_${it.id}" }) { task ->
                TodayTaskCard(
                    task = task,
                    onToggle = { onToggleTask(task) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Footer Notice
        item {
            InfoNoticeCard(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
fun TodayHeroCard(
    dueTodayCount: Int,
    todayExpenseTotal: Double,
    nextAppointment: AppointmentEntity?,
    currencySymbol: String,
    onAddExpenseClick: () -> Unit,
    onAddBillClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good day! 👋",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = DateTimeUtils.formatDate(System.currentTimeMillis(), "EEEE, dd MMMM yyyy"),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (dueTodayCount > 0) StatusDueTodayBg else StatusPaidBg
                ) {
                    Text(
                        text = if (dueTodayCount > 0) "🔴 $dueTodayCount Due Today" else "✓ All Caught Up",
                        color = if (dueTodayCount > 0) StatusDueToday else StatusPaid,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Today's Spent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = DateTimeUtils.formatCurrency(todayExpenseTotal, currencySymbol),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Next Agenda", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = nextAppointment?.let { "${it.title.take(16)}..." } ?: "No meetings today",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TodayDueBillCard(
    bill: BillEntity,
    currencySymbol: String,
    onPayClick: () -> Unit,
    onSnoozeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOverdue = DateTimeUtils.isOverdue(bill.dueDateMillis)
    val cat = BillCategory.fromString(bill.category)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) Color(0xFFFFF1F2) else Color(0xFFFEF2F2)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isOverdue) Color(0xFFFDA4AF) else Color(0xFFFECDD3))
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFE4E6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(cat.iconEmoji, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = bill.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF9F1239)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isOverdue) "⚠️ Overdue" else "🔴 Due Today",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFBE123C),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            if (bill.familyMember.isNotBlank() && bill.familyMember != "Family") {
                                Text(" • For ${bill.familyMember}", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF881337)))
                            }
                        }
                    }
                }

                Text(
                    text = DateTimeUtils.formatCurrency(bill.amount, currencySymbol),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9F1239)
                    )
                )
            }

            if (bill.notes.isNotBlank() || bill.docPolicyNo.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = listOf(bill.docPolicyNo, bill.notes).filter { it.isNotBlank() }.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF881337).copy(alpha = 0.8f)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Mark Paid & Snooze
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSnoozeClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9F1239)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Snooze, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Remind Tomorrow", fontSize = 12.sp)
                }

                Button(
                    onClick = onPayClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusPaid),
                    modifier = Modifier.weight(1f).testTag("mark_paid_${bill.id}")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mark Paid ✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun TodayExpenseSummaryCard(
    totalAmount: Double,
    expenses: List<ExpenseEntity>,
    currencySymbol: String,
    onAddClick: () -> Unit,
    onViewHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Spent Today", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = DateTimeUtils.formatCurrency(totalAmount, currencySymbol),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                FilledTonalButton(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Expense", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (expenses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                expenses.take(4).forEach { item ->
                    val cat = ExpenseCategory.fromString(item.category)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(cat.iconEmoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = DateTimeUtils.formatCurrency(item.amount, currencySymbol),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "No expenses recorded today yet (e.g. Milk, Groceries, Fuel)",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
    }
}

@Composable
fun UpcomingBillItemRow(
    bill: BillEntity,
    currencySymbol: String,
    onPayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cat = BillCategory.fromString(bill.category)
    val relativeDate = DateTimeUtils.getRelativeDateString(bill.dueDateMillis)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(cat.iconEmoji, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = bill.name,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Due $relativeDate • ${bill.frequency}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = DateTimeUtils.formatCurrency(bill.amount, currencySymbol),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                TextButton(
                    onClick = onPayClick,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Pay ✓", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusPaid)
                }
            }
        }
    }
}

@Composable
fun TodayAppointmentCard(
    appointment: AppointmentEntity,
    modifier: Modifier = Modifier
) {
    val cat = AppointmentCategory.fromString(appointment.category)
    val formattedTime = DateTimeUtils.formatDateTime(appointment.dateTimeMillis)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TealSecondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(cat.iconEmoji, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${appointment.personOrLocation} • $formattedTime",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                if (appointment.familyMember.isNotBlank()) {
                    Text(
                        text = "For: ${appointment.familyMember}",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
fun TodayTaskCard(
    task: RecurringTaskEntity,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompletedToday) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Checkbox(
                    checked = task.isCompletedToday,
                    onCheckedChange = { onToggle() }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (task.isCompletedToday) FontWeight.Normal else FontWeight.SemiBold,
                            textDecoration = if (task.isCompletedToday) TextDecoration.LineThrough else null,
                            color = if (task.isCompletedToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "${task.category} • 🔥 ${task.streakCount} day streak",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}
