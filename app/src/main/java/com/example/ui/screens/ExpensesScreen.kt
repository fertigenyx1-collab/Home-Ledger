package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import java.util.Calendar

@Composable
fun ExpensesScreen(
    expenses: List<ExpenseEntity>,
    familyMembers: List<FamilyMemberEntity>,
    selectedFamily: String,
    periodFilter: String, // Daily, Weekly, Monthly, Quarterly, Half-Yearly, Yearly
    currencySymbol: String,
    onFamilySelect: (String) -> Unit,
    onPeriodSelect: (String) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onAddExpenseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val familyFiltered = if (selectedFamily == "ALL") expenses else expenses.filter {
        it.familyMember.equals(selectedFamily, ignoreCase = true) || it.familyMember == "Family"
    }

    // Filter by period
    val now = Calendar.getInstance()
    val periodFiltered = familyFiltered.filter { exp ->
        val expCal = Calendar.getInstance().apply { timeInMillis = exp.dateMillis }
        when (periodFilter) {
            "Daily" -> DateTimeUtils.isToday(exp.dateMillis)
            "Weekly" -> DateTimeUtils.isWithinNextDays(exp.dateMillis - 86400000L * 7, 7) ||
                    (now.get(Calendar.WEEK_OF_YEAR) == expCal.get(Calendar.WEEK_OF_YEAR) && now.get(Calendar.YEAR) == expCal.get(Calendar.YEAR))
            "Monthly" -> now.get(Calendar.MONTH) == expCal.get(Calendar.MONTH) && now.get(Calendar.YEAR) == expCal.get(Calendar.YEAR)
            "Quarterly" -> {
                val currentQuarter = now.get(Calendar.MONTH) / 3
                val expQuarter = expCal.get(Calendar.MONTH) / 3
                currentQuarter == expQuarter && now.get(Calendar.YEAR) == expCal.get(Calendar.YEAR)
            }
            "Half-Yearly" -> {
                val currentHalf = now.get(Calendar.MONTH) / 6
                val expHalf = expCal.get(Calendar.MONTH) / 6
                currentHalf == expHalf && now.get(Calendar.YEAR) == expCal.get(Calendar.YEAR)
            }
            "Yearly" -> now.get(Calendar.YEAR) == expCal.get(Calendar.YEAR)
            else -> true
        }
    }

    val totalSpent = periodFiltered.sumOf { it.amount }

    // Category breakdown
    val categoryTotals = periodFiltered.groupBy { it.category }
        .mapValues { (_, list) -> list.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpenseClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 70.dp).testTag("add_expense_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("expenses_screen_list"),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Family Filter Bar
            item {
                FamilyFilterRow(
                    members = familyMembers,
                    selectedMember = selectedFamily,
                    onSelectMember = onFamilySelect
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Period Selector Tabs (Daily, Weekly, Monthly, Quarterly, Half-Yearly, Yearly)
            item {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Daily", "Weekly", "Monthly", "Quarterly", "Half-Yearly", "Yearly").forEach { period ->
                        FilterChip(
                            selected = periodFilter == period,
                            onClick = { onPeriodSelect(period) },
                            label = { Text(period, fontWeight = if (periodFilter == period) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Overview Total Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Total $periodFilter Spending",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = DateTimeUtils.formatCurrency(totalSpent, currencySymbol),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${periodFiltered.size} expense entries recorded",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Category Breakdown Section
            if (categoryTotals.isNotEmpty()) {
                item {
                    SectionHeader(title = "📊 Category Distribution")
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            categoryTotals.forEach { (catName, amount) ->
                                val cat = ExpenseCategory.fromString(catName)
                                val pct = if (totalSpent > 0) (amount / totalSpent) else 0.0
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(cat.iconEmoji, fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(cat.displayName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                        }
                                        Text(
                                            text = "${DateTimeUtils.formatCurrency(amount, currencySymbol)} (${(pct * 100).toInt()}%)",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { pct.toFloat() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // Transaction History
            item {
                SectionHeader(title = "📝 Spending History ($periodFilter)")
            }

            if (periodFiltered.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = "🛒",
                        title = "No Expenses in $periodFilter",
                        description = "Log your groceries, milk, vegetables, fuel, or medical purchases.",
                        actionButtonText = "Add Expense",
                        onActionClick = onAddExpenseClick
                    )
                }
            } else {
                items(periodFiltered, key = { it.id }) { expense ->
                    ExpenseHistoryRow(
                        expense = expense,
                        currencySymbol = currencySymbol,
                        onDeleteClick = { onDeleteExpense(expense) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseHistoryRow(
    expense: ExpenseEntity,
    currencySymbol: String,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cat = ExpenseCategory.fromString(expense.category)

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
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(cat.iconEmoji, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = expense.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${DateTimeUtils.formatDate(expense.dateMillis)} • ${expense.familyMember}" +
                                if (expense.notes.isNotBlank()) " • ${expense.notes}" else "",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = DateTimeUtils.formatCurrency(expense.amount, currencySymbol),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
