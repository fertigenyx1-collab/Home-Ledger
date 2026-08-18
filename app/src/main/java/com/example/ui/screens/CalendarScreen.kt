package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.util.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarScreen(
    bills: List<BillEntity>,
    expenses: List<ExpenseEntity>,
    appointments: List<AppointmentEntity>,
    tasks: List<RecurringTaskEntity>,
    selectedDateMillis: Long,
    currencySymbol: String,
    onSelectDate: (Long) -> Unit,
    onPayBillClick: (BillEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var calendarMonth by remember {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        cal.set(Calendar.DAY_OF_MONTH, 1)
        mutableStateOf(cal)
    }

    val selectedCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }

    // Filter items for the selected exact day
    val dayBills = bills.filter { DateTimeUtils.isSameDay(it.dueDateMillis, selectedDateMillis) }
    val dayExpenses = expenses.filter { DateTimeUtils.isSameDay(it.dateMillis, selectedDateMillis) }
    val dayAppointments = appointments.filter { DateTimeUtils.isSameDay(it.dateTimeMillis, selectedDateMillis) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen_list"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Month Navigation Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            val newCal = (calendarMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                            calendarMonth = newCal
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                        }

                        val monthTitle = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendarMonth.time)
                        Text(
                            text = monthTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        IconButton(onClick = {
                            val newCal = (calendarMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                            calendarMonth = newCal
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Days of week header
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayName ->
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Grid Days
                    val daysInMonth = calendarMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val firstDayOfWeek = (calendarMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.DAY_OF_WEEK) - 1

                    val totalSlots = firstDayOfWeek + daysInMonth
                    val rows = (totalSlots + 6) / 7

                    Column {
                        for (r in 0 until rows) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                for (c in 0..6) {
                                    val dayIndex = r * 7 + c
                                    val dayOfMonth = dayIndex - firstDayOfWeek + 1

                                    if (dayOfMonth in 1..daysInMonth) {
                                        val dayCal = (calendarMonth.clone() as Calendar).apply {
                                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                        }
                                        val dayMillis = dayCal.timeInMillis
                                        val isSelected = selectedCal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                                                selectedCal.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)

                                        val hasBills = bills.any { DateTimeUtils.isSameDay(it.dueDateMillis, dayMillis) }
                                        val hasAppts = appointments.any { DateTimeUtils.isSameDay(it.dateTimeMillis, dayMillis) }
                                        val hasExpenses = expenses.any { DateTimeUtils.isSameDay(it.dateMillis, dayMillis) }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(2.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary
                                                    else Color.Transparent
                                                )
                                                .clickable { onSelectDate(dayMillis) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = dayOfMonth.toString(),
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                                // Indicator dots
                                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    if (hasBills) {
                                                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else StatusDueToday))
                                                    }
                                                    if (hasAppts) {
                                                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else TealSecondary))
                                                    }
                                                    if (hasExpenses) {
                                                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else AmberTertiary))
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Day Agenda Header
        item {
            SectionHeader(
                title = "Agenda for ${DateTimeUtils.formatDate(selectedDateMillis, "dd MMMM yyyy")}"
            )
        }

        if (dayBills.isEmpty() && dayAppointments.isEmpty() && dayExpenses.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "No bills, appointments, or recorded expenses on this day.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // Agenda Items: Bills
        if (dayBills.isNotEmpty()) {
            item {
                Text(
                    text = "🔔 Bills Due (${dayBills.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            items(dayBills, key = { "cal_bill_${it.id}" }) { bill ->
                UpcomingBillItemRow(
                    bill = bill,
                    currencySymbol = currencySymbol,
                    onPayClick = { onPayBillClick(bill) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // Agenda Items: Appointments
        if (dayAppointments.isNotEmpty()) {
            item {
                Text(
                    text = "🏥 Appointments (${dayAppointments.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            items(dayAppointments, key = { "cal_appt_${it.id}" }) { appt ->
                TodayAppointmentCard(
                    appointment = appt,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // Agenda Items: Expenses
        if (dayExpenses.isNotEmpty()) {
            item {
                Text(
                    text = "💰 Expenses Recorded (${DateTimeUtils.formatCurrency(dayExpenses.sumOf { it.amount }, currencySymbol)})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            items(dayExpenses, key = { "cal_exp_${it.id}" }) { expense ->
                ExpenseHistoryRow(
                    expense = expense,
                    currencySymbol = currencySymbol,
                    onDeleteClick = {},
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}
