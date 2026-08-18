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
fun AppointmentsScreen(
    appointments: List<AppointmentEntity>,
    familyMembers: List<FamilyMemberEntity>,
    selectedFamily: String,
    selectedCategory: String,
    onFamilySelect: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onStatusChange: (AppointmentEntity, AppointmentStatus) -> Unit,
    onDeleteAppointment: (AppointmentEntity) -> Unit,
    onAddAppointmentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val familyFiltered = if (selectedFamily == "ALL") appointments else appointments.filter {
        it.familyMember.equals(selectedFamily, ignoreCase = true)
    }

    val categoryFiltered = if (selectedCategory == "ALL") familyFiltered else familyFiltered.filter {
        it.category.equals(selectedCategory, ignoreCase = true)
    }

    val upcomingList = categoryFiltered.filter { it.status == AppointmentStatus.UPCOMING.name }
    val completedList = categoryFiltered.filter { it.status == AppointmentStatus.COMPLETED.name }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAppointmentClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 70.dp).testTag("add_appointment_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Appointment")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("appointments_screen_list"),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Family Filter
            item {
                FamilyFilterRow(
                    members = familyMembers,
                    selectedMember = selectedFamily,
                    onSelectMember = onFamilySelect
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Category Filter Chips (Hospital, School, Work, Personal, Vehicle)
            item {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == "ALL",
                        onClick = { onCategorySelect("ALL") },
                        label = { Text("All Categories") }
                    )
                    AppointmentCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.name,
                            onClick = { onCategorySelect(cat.name) },
                            label = { Text("${cat.iconEmoji} ${cat.displayName}") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Upcoming Section
            if (upcomingList.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Upcoming Appointments (${upcomingList.size})",
                        badgeCount = upcomingList.size
                    )
                }
                items(upcomingList, key = { it.id }) { appt ->
                    AppointmentDetailCard(
                        appointment = appt,
                        onMarkCompleted = { onStatusChange(appt, AppointmentStatus.COMPLETED) },
                        onDelete = { onDeleteAppointment(appt) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // Completed Section
            if (completedList.isNotEmpty()) {
                item {
                    SectionHeader(title = "Completed Past Appointments (${completedList.size})")
                }
                items(completedList, key = { it.id }) { appt ->
                    AppointmentDetailCard(
                        appointment = appt,
                        onMarkCompleted = {},
                        onDelete = { onDeleteAppointment(appt) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            if (categoryFiltered.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = "🏥",
                        title = "No Appointments Found",
                        description = "Schedule doctor consultations, school PTMs, vehicle servicing, or business meetings with automated reminders.",
                        actionButtonText = "Add Appointment",
                        onActionClick = onAddAppointmentClick
                    )
                }
            }
        }
    }
}

@Composable
fun AppointmentDetailCard(
    appointment: AppointmentEntity,
    onMarkCompleted: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cat = AppointmentCategory.fromString(appointment.category)
    val isCompleted = appointment.status == AppointmentStatus.COMPLETED.name
    val formattedDateTime = DateTimeUtils.formatDateTime(appointment.dateTimeMillis)
    val relativeDate = DateTimeUtils.getRelativeDateString(appointment.dateTimeMillis)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
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
                            .background(TealSecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(cat.iconEmoji, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = appointment.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "📍 ${appointment.personOrLocation}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                        )
                    }
                }

                StatusBadge(
                    text = if (isCompleted) "Completed ✓" else relativeDate,
                    backgroundColor = if (isCompleted) StatusPaidBg else StatusInfoBg,
                    textColor = if (isCompleted) StatusPaid else StatusInfo
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "⏰ $formattedDateTime",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                )
                if (appointment.familyMember.isNotBlank()) {
                    Text(
                        text = "For: ${appointment.familyMember}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            if (appointment.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Notes: ${appointment.notes}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }

                if (!isCompleted) {
                    Button(
                        onClick = onMarkCompleted,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealSecondary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Completed ✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
