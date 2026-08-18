package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.util.DateTimeUtils

@Composable
fun BillsScreen(
    bills: List<BillEntity>,
    familyMembers: List<FamilyMemberEntity>,
    selectedFamily: String,
    filterStatus: String,
    currencySymbol: String,
    onFamilySelect: (String) -> Unit,
    onFilterStatusSelect: (String) -> Unit,
    onPayBillClick: (BillEntity) -> Unit,
    onSnoozeBillClick: (BillEntity) -> Unit,
    onDeleteBill: (BillEntity) -> Unit,
    onAddBillClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val familyFiltered = if (selectedFamily == "ALL") bills else bills.filter {
        it.familyMember.equals(selectedFamily, ignoreCase = true) || it.familyMember == "Family"
    }

    val categoryFiltered = if (selectedCategoryFilter == "ALL") familyFiltered else familyFiltered.filter {
        it.category.equals(selectedCategoryFilter, ignoreCase = true)
    }

    val finalBills = when (filterStatus) {
        "PENDING" -> categoryFiltered.filter { it.status == BillStatus.PENDING.name }
        "OVERDUE" -> categoryFiltered.filter { it.status == BillStatus.PENDING.name && DateTimeUtils.isOverdue(it.dueDateMillis) }
        "PAID" -> categoryFiltered.filter { it.status == BillStatus.PAID.name }
        "RECURRING" -> categoryFiltered.filter { it.frequency != Frequency.ONE_TIME.name }
        else -> categoryFiltered
    }

    val pendingTotal = familyFiltered.filter { it.status == BillStatus.PENDING.name }.sumOf { it.amount }
    val paidTotal = familyFiltered.filter { it.status == BillStatus.PAID.name }.sumOf { it.amount }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBillClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 70.dp).testTag("add_bill_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Bill Reminder")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("bills_screen_list"),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Family Filter Row
            item {
                FamilyFilterRow(
                    members = familyMembers,
                    selectedMember = selectedFamily,
                    onSelectMember = onFamilySelect
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Summary Stats Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pending Obligations", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = DateTimeUtils.formatCurrency(pendingTotal, currencySymbol),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = AmberTertiary)
                            )
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("Paid This Cycle", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = DateTimeUtils.formatCurrency(paidTotal, currencySymbol),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = StatusPaid)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Status Filter Chips
            item {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL" to "All", "PENDING" to "Pending", "OVERDUE" to "Overdue", "PAID" to "Paid", "RECURRING" to "Recurring").forEach { (key, label) ->
                        FilterChip(
                            selected = filterStatus == key,
                            onClick = { onFilterStatusSelect(key) },
                            label = { Text(label, fontWeight = if (filterStatus == key) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Category Filter Chips
            item {
                val catScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(catScrollState)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCategoryFilter == "ALL",
                        onClick = { selectedCategoryFilter = "ALL" },
                        label = { Text("All Categories") }
                    )
                    BillCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat.name,
                            onClick = { selectedCategoryFilter = cat.name },
                            label = { Text("${cat.iconEmoji} ${cat.displayName}") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (finalBills.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = "🔔",
                        title = "No Bills Found",
                        description = "Add recurring or one-time bills (Electricity, Rent, Insurance, LIC, Mobile Recharge) to get timely reminders.",
                        actionButtonText = "Add Bill Reminder",
                        onActionClick = onAddBillClick
                    )
                }
            } else {
                items(finalBills, key = { it.id }) { bill ->
                    BillCardItem(
                        bill = bill,
                        currencySymbol = currencySymbol,
                        onPayClick = { onPayBillClick(bill) },
                        onSnoozeClick = { onSnoozeBillClick(bill) },
                        onDeleteClick = { onDeleteBill(bill) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BillCardItem(
    bill: BillEntity,
    currencySymbol: String,
    onPayClick: () -> Unit,
    onSnoozeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cat = BillCategory.fromString(bill.category)
    val freq = Frequency.fromString(bill.frequency)
    val isPaid = bill.status == BillStatus.PAID.name
    val isOverdue = !isPaid && DateTimeUtils.isOverdue(bill.dueDateMillis)
    val isDueToday = !isPaid && DateTimeUtils.isToday(bill.dueDateMillis)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isPaid -> StatusPaidBg.copy(alpha = 0.4f)
                isOverdue -> Color(0xFFFFF1F2)
                isDueToday -> Color(0xFFFEF2F2)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                when {
                    isPaid -> StatusPaid.copy(alpha = 0.3f)
                    isOverdue -> Color(0xFFFDA4AF)
                    isDueToday -> Color(0xFFFECDD3)
                    else -> MaterialTheme.colorScheme.outlineVariant
                }
            )
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
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(cat.iconEmoji, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = bill.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🔄 ${freq.displayName}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            )
                            if (bill.familyMember.isNotBlank()) {
                                Text(" • ${bill.familyMember}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = DateTimeUtils.formatCurrency(bill.amount, currencySymbol),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isPaid) StatusPaid else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    StatusBadge(
                        text = when {
                            isPaid -> "Paid ✓"
                            isOverdue -> "Overdue"
                            isDueToday -> "Due Today"
                            else -> "Due ${DateTimeUtils.getRelativeDateString(bill.dueDateMillis)}"
                        },
                        backgroundColor = when {
                            isPaid -> StatusPaidBg
                            isOverdue -> StatusDueTodayBg
                            isDueToday -> StatusDueTodayBg
                            else -> StatusUpcomingBg
                        },
                        textColor = when {
                            isPaid -> StatusPaid
                            isOverdue -> StatusDueToday
                            isDueToday -> StatusDueToday
                            else -> StatusUpcoming
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Due Date: ${DateTimeUtils.formatDate(bill.dueDateMillis)}" +
                            if (bill.docPolicyNo.isNotBlank()) " • Ref: ${bill.docPolicyNo}" else "",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "⏰ ${bill.reminderDaysBefore}d before",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            if (bill.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Note: ${bill.notes}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isPaid) {
                        OutlinedButton(
                            onClick = onSnoozeClick,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Snooze", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onPayClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusPaid),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(34.dp).testTag("bill_pay_${bill.id}")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark as Paid ✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else {
                        Text(
                            text = "✓ Paid on ${DateTimeUtils.formatDate(bill.lastPaidDateMillis ?: bill.dueDateMillis)}",
                            style = MaterialTheme.typography.labelMedium.copy(color = StatusPaid, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
