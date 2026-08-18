package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun TasksScreen(
    tasks: List<RecurringTaskEntity>,
    familyMembers: List<FamilyMemberEntity>,
    selectedFamily: String,
    selectedFrequency: String,
    onFamilySelect: (String) -> Unit,
    onFrequencySelect: (String) -> Unit,
    onToggleTask: (RecurringTaskEntity) -> Unit,
    onDeleteTask: (RecurringTaskEntity) -> Unit,
    onAddTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val familyFiltered = if (selectedFamily == "ALL") tasks else tasks.filter {
        it.familyMember.equals(selectedFamily, ignoreCase = true) || it.familyMember == "Family"
    }

    val filteredTasks = if (selectedFrequency == "ALL") familyFiltered else familyFiltered.filter {
        it.frequency.equals(selectedFrequency, ignoreCase = true)
    }

    val completedCount = filteredTasks.count { it.isCompletedToday }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 70.dp).testTag("add_task_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Recurring Task")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("tasks_screen_list"),
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

            // Frequency Filter Tabs (Daily, Weekly, Monthly, Quarterly, Half-Yearly, Yearly)
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
                        selected = selectedFrequency == "ALL",
                        onClick = { onFrequencySelect("ALL") },
                        label = { Text("All Frequencies") }
                    )
                    TaskFrequency.entries.forEach { freq ->
                        FilterChip(
                            selected = selectedFrequency == freq.name,
                            onClick = { onFrequencySelect(freq.name) },
                            label = { Text(freq.displayName, fontWeight = if (selectedFrequency == freq.name) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Progress Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Recurring Tasks Completion", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("$completedCount / ${filteredTasks.size} Done", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val progress = if (filteredTasks.isNotEmpty()) (completedCount.toFloat() / filteredTasks.size) else 0f
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = StatusPaid,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (filteredTasks.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = "🔄",
                        title = "No Tasks Found",
                        description = "Set up recurring routines: daily milk intake, weekly grocery restock, vehicle oil check, or yearly insurance audits.",
                        actionButtonText = "Add Recurring Task",
                        onActionClick = onAddTaskClick
                    )
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskCardItem(
                        task = task,
                        onToggle = { onToggleTask(task) },
                        onDelete = { onDeleteTask(task) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCardItem(
    task: RecurringTaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompletedToday) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Checkbox(
                    checked = task.isCompletedToday,
                    onCheckedChange = { onToggle() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (task.isCompletedToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🔄 ${task.frequency} • ${task.category}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🔥 ${task.streakCount} streaks",
                            style = MaterialTheme.typography.bodySmall.copy(color = AmberTertiary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        )
                    }
                    if (task.notes.isNotBlank()) {
                        Text(
                            text = task.notes,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        )
                    }
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
            }
        }
    }
}
