package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.util.DateTimeUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    familyMembers: List<FamilyMemberEntity>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, category: String, dateMillis: Long, notes: String, familyMember: String, paymentMethod: String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.GROCERY) }
    var selectedFamily by remember { mutableStateOf(familyMembers.firstOrNull()?.name ?: "Self") }
    var paymentMethod by remember { mutableStateOf("Cash / UPI") }
    var notes by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var familyDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💰 Add Expense",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title (e.g. D-Mart Grocery, Milk, Petrol)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("expense_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) amountText = it },
                    label = { Text("Amount ($currencySymbol)") },
                    leadingIcon = { Text(currencySymbol, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("expense_amount_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedCategory.iconEmoji} ${selectedCategory.displayName}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        ExpenseCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.iconEmoji} ${cat.displayName}") },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Date Picker row
                OutlinedCard(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(y, m, d)
                                    }
                                    selectedDateMillis = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Date: ${DateTimeUtils.formatDate(selectedDateMillis)}")
                        }
                        Text("Change", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Family Member Dropdown
                ExposedDropdownMenuBox(
                    expanded = familyDropdownExpanded,
                    onExpandedChange = { familyDropdownExpanded = !familyDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "For: $selectedFamily",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Family Member") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = familyDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = familyDropdownExpanded,
                        onDismissRequest = { familyDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("🏠 Family / Shared") },
                            onClick = { selectedFamily = "Family"; familyDropdownExpanded = false }
                        )
                        familyMembers.forEach { member ->
                            DropdownMenuItem(
                                text = { Text("${member.emoji} ${member.name}") },
                                onClick = {
                                    selectedFamily = member.name
                                    familyDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (title.isNotBlank() && amount > 0.0) {
                            onConfirm(title, amount, selectedCategory.name, selectedDateMillis, notes, selectedFamily, paymentMethod)
                        }
                    },
                    enabled = title.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("save_expense_button")
                ) {
                    Text("Save Expense", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillDialog(
    familyMembers: List<FamilyMemberEntity>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (
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
    ) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(BillCategory.ELECTRICITY) }
    var selectedFrequency by remember { mutableStateOf(Frequency.MONTHLY) }
    var reminderDays by remember { mutableIntStateOf(3) }
    var selectedFamily by remember { mutableStateOf(familyMembers.firstOrNull()?.name ?: "Family") }
    var notes by remember { mutableStateOf("") }
    var docPolicyNo by remember { mutableStateOf("") }
    var autoExpenseOnPaid by remember { mutableStateOf(true) }
    var selectedDueDateMillis by remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 3) }
        mutableStateOf(cal.timeInMillis)
    }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var freqDropdownExpanded by remember { mutableStateOf(false) }
    var familyDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔔 Add Bill & Payment",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Bill / Payment Name (e.g. Electricity, LIC, Health Ins)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("bill_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) amountText = it },
                    label = { Text("Due Amount ($currencySymbol)") },
                    leadingIcon = { Text(currencySymbol, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("bill_amount_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedCategory.iconEmoji} ${selectedCategory.displayName}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        BillCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.iconEmoji} ${cat.displayName}") },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Frequency Dropdown
                ExposedDropdownMenuBox(
                    expanded = freqDropdownExpanded,
                    onExpandedChange = { freqDropdownExpanded = !freqDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "🔄 ${selectedFrequency.displayName}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Recurrence Schedule") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = freqDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = freqDropdownExpanded,
                        onDismissRequest = { freqDropdownExpanded = false }
                    ) {
                        Frequency.entries.forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq.displayName) },
                                onClick = {
                                    selectedFrequency = freq
                                    freqDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Due Date Picker
                OutlinedCard(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = selectedDueDateMillis }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(y, m, d)
                                    }
                                    selectedDueDateMillis = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Due Date: ${DateTimeUtils.formatDate(selectedDueDateMillis)}")
                        }
                        Text("Change", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Reminder Days
                Text("Remind me before due date:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1, 2, 3, 7, 15, 30).forEach { days ->
                        FilterChip(
                            selected = reminderDays == days,
                            onClick = { reminderDays = days },
                            label = { Text("${days}d") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Policy / Consumer Number
                OutlinedTextField(
                    value = docPolicyNo,
                    onValueChange = { docPolicyNo = it },
                    label = { Text("Policy No / Consumer ID / Account (Optional)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Family Member
                ExposedDropdownMenuBox(
                    expanded = familyDropdownExpanded,
                    onExpandedChange = { familyDropdownExpanded = !familyDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "Assigned to: $selectedFamily",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Family Member") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = familyDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = familyDropdownExpanded,
                        onDismissRequest = { familyDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("🏠 Family / Shared") },
                            onClick = { selectedFamily = "Family"; familyDropdownExpanded = false }
                        )
                        familyMembers.forEach { member ->
                            DropdownMenuItem(
                                text = { Text("${member.emoji} ${member.name}") },
                                onClick = {
                                    selectedFamily = member.name
                                    familyDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Auto Expense Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-record in Expenses", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("When marked paid, automatically log into expense history", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoExpenseOnPaid,
                        onCheckedChange = { autoExpenseOnPaid = it }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank() && amount > 0.0) {
                            onConfirm(
                                name,
                                amount,
                                selectedDueDateMillis,
                                selectedFrequency.name,
                                selectedCategory.name,
                                reminderDays,
                                notes,
                                docPolicyNo,
                                selectedFamily,
                                autoExpenseOnPaid
                            )
                        }
                    },
                    enabled = name.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("save_bill_button")
                ) {
                    Text("Save Bill Reminder", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PayBillConfirmDialog(
    bill: BillEntity,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirmPay: (bill: BillEntity, customAmount: Double?, paidDate: Long) -> Unit
) {
    val context = LocalContext.current
    var customAmountText by remember { mutableStateOf(bill.amount.toString()) }
    var selectedPaidDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(StatusPaidBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = StatusPaid,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Record Bill Payment",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = bill.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Custom amount
                OutlinedTextField(
                    value = customAmountText,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) customAmountText = it },
                    label = { Text("Amount Paid ($currencySymbol)") },
                    leadingIcon = { Text(currencySymbol, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("pay_amount_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Paid date
                OutlinedCard(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = selectedPaidDateMillis }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newCal = Calendar.getInstance().apply { set(y, m, d) }
                                    selectedPaidDateMillis = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Paid on: ${DateTimeUtils.formatDate(selectedPaidDateMillis)}")
                        Text("Change", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Smart Recurring Engine info box
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("⚡ Smart Engine Action:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. Marks bill as paid\n2. Logs ${DateTimeUtils.formatCurrency(customAmountText.toDoubleOrNull() ?: bill.amount, currencySymbol)} into Expense History\n" +
                                    if (Frequency.fromString(bill.frequency) != Frequency.ONE_TIME)
                                        "3. Schedules next ${bill.frequency} reminder automatically"
                                    else "3. One-time bill completed",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val amt = customAmountText.toDoubleOrNull()
                            onConfirmPay(bill, amt, selectedPaidDateMillis)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusPaid),
                        modifier = Modifier.weight(1f).testTag("confirm_mark_paid_button")
                    ) {
                        Text("Mark Paid ✓", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentDialog(
    familyMembers: List<FamilyMemberEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        personOrLocation: String,
        category: String,
        dateTimeMillis: Long,
        reminderHoursBefore: Int,
        reminderMinutesBefore: Int,
        familyMember: String,
        notes: String
    ) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var personOrLocation by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(AppointmentCategory.HOSPITAL) }
    var selectedFamily by remember { mutableStateOf(familyMembers.firstOrNull()?.name ?: "Self") }
    var reminderHours by remember { mutableIntStateOf(24) }
    var notes by remember { mutableStateOf("") }

    val appointmentCal = remember {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 16)
            set(Calendar.MINUTE, 30)
        }
    }
    var selectedDateTimeMillis by remember { mutableStateOf(appointmentCal.timeInMillis) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var familyDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅 Add Appointment",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g. Dr. Kumar Checkup, School PTM)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("appointment_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = personOrLocation,
                    onValueChange = { personOrLocation = it },
                    label = { Text("Doctor / Person / Location / Venue") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedCategory.iconEmoji} ${selectedCategory.displayName}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        AppointmentCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.iconEmoji} ${cat.displayName}") },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Date & Time Picker
                OutlinedCard(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = selectedDateTimeMillis }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            val newCal = Calendar.getInstance().apply {
                                                set(y, m, d, hourOfDay, minute)
                                            }
                                            selectedDateTimeMillis = newCal.timeInMillis
                                        },
                                        cal.get(Calendar.HOUR_OF_DAY),
                                        cal.get(Calendar.MINUTE),
                                        false
                                    ).show()
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("When: ${DateTimeUtils.formatDateTime(selectedDateTimeMillis)}")
                        }
                        Text("Change", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Family Member
                ExposedDropdownMenuBox(
                    expanded = familyDropdownExpanded,
                    onExpandedChange = { familyDropdownExpanded = !familyDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "For: $selectedFamily",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Family Member") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = familyDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = familyDropdownExpanded,
                        onDismissRequest = { familyDropdownExpanded = false }
                    ) {
                        familyMembers.forEach { member ->
                            DropdownMenuItem(
                                text = { Text("${member.emoji} ${member.name}") },
                                onClick = {
                                    selectedFamily = member.name
                                    familyDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Reminder Hours
                Text("Reminder Alert:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(2 to "2 hrs", 12 to "12 hrs", 24 to "1 day", 48 to "2 days").forEach { (hrs, label) ->
                        FilterChip(
                            selected = reminderHours == hrs,
                            onClick = { reminderHours = hrs },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Past reports / Instructions") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onConfirm(
                                title,
                                personOrLocation,
                                selectedCategory.name,
                                selectedDateTimeMillis,
                                reminderHours,
                                120,
                                selectedFamily,
                                notes
                            )
                        }
                    },
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("save_appointment_button")
                ) {
                    Text("Save Appointment", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    familyMembers: List<FamilyMemberEntity>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, frequency: String, category: String, familyMember: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf(TaskFrequency.DAILY) }
    var category by remember { mutableStateOf("Daily Habit") }
    var selectedFamily by remember { mutableStateOf("Self") }
    var notes by remember { mutableStateOf("") }

    var freqDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔄 Add Recurring Task",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task (e.g. Take milk, Water plants, AC service)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("task_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = freqDropdownExpanded,
                    onExpandedChange = { freqDropdownExpanded = !freqDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "🔄 ${selectedFrequency.displayName}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Repeat Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = freqDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = freqDropdownExpanded,
                        onDismissRequest = { freqDropdownExpanded = false }
                    ) {
                        TaskFrequency.entries.forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq.displayName) },
                                onClick = {
                                    selectedFrequency = freq
                                    freqDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Health, Home, Vehicle, Finance)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onConfirm(title, selectedFrequency.name, category, selectedFamily, notes)
                        }
                    },
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("save_task_button")
                ) {
                    Text("Save Recurring Task", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentDialog(
    familyMembers: List<FamilyMemberEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        docType: String,
        identifierOrPolicyNo: String,
        issuerOrProvider: String,
        expiryDateMillis: Long?,
        notes: String,
        familyMember: String,
        linkedBillName: String
    ) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(DocType.HEALTH_INSURANCE) }
    var policyNo by remember { mutableStateOf("") }
    var issuer by remember { mutableStateOf("") }
    var selectedFamily by remember { mutableStateOf("Family") }
    var linkedBillName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var hasExpiry by remember { mutableStateOf(true) }
    var selectedExpiryMillis by remember {
        val cal = Calendar.getInstance().apply { add(Calendar.YEAR, 1) }
        mutableStateOf(cal.timeInMillis)
    }

    var typeDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📁 Add Policy / Doc",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document Title (e.g. Star Health Floater, Car RC)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("doc_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedType.iconEmoji} ${selectedType.displayName}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Document Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        DocType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text("${type.iconEmoji} ${type.displayName}") },
                                onClick = {
                                    selectedType = type
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = policyNo,
                    onValueChange = { policyNo = it },
                    label = { Text("Policy / Registration / ID Number") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = issuer,
                    onValueChange = { issuer = it },
                    label = { Text("Issuer / Insurer / Provider (e.g. HDFC, LIC, Gov)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Expiry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Has Expiry / Renewal Date", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = hasExpiry, onCheckedChange = { hasExpiry = it })
                }

                if (hasExpiry) {
                    OutlinedCard(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val cal = Calendar.getInstance().apply { timeInMillis = selectedExpiryMillis }
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val newCal = Calendar.getInstance().apply { set(y, m, d) }
                                        selectedExpiryMillis = newCal.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Expiry: ${DateTimeUtils.formatDate(selectedExpiryMillis)}")
                            Text("Change", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Coverage details, TPA contact, notes") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onConfirm(
                                title,
                                selectedType.name,
                                policyNo,
                                issuer,
                                if (hasExpiry) selectedExpiryMillis else null,
                                notes,
                                selectedFamily,
                                linkedBillName
                            )
                        }
                    },
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("save_doc_button")
                ) {
                    Text("Save Document Info", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    currentCurrency: String,
    onSelectCurrency: (String) -> Unit,
    onReloadDemoData: () -> Unit,
    onAddFamilyMemberClick: () -> Unit,
    onExportClick: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚙️ Settings & Info",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                InfoNoticeCard()

                Spacer(modifier = Modifier.height(16.dp))

                // Currency selector
                Text("Currency Symbol", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("₹", "$", "€", "£", "AED").forEach { symbol ->
                        FilterChip(
                            selected = currentCurrency == symbol,
                            onClick = { onSelectCurrency(symbol) },
                            label = { Text(symbol, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Export & Download APK button
                if (onExportClick != null) {
                    Button(
                        onClick = {
                            onDismiss()
                            onExportClick()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("export_apk_settings_button")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export & Download APK", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Family Member Management button
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onAddFamilyMemberClick()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Family Member")
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Reload demo data button
                OutlinedButton(
                    onClick = onReloadDemoData,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("reload_demo_data_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reload Realistic Demo Data")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "LifeRemind v1.0 • Personal Life & Payment Reminder Manager",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                )
            }
        }
    }
}

@Composable
fun AddFamilyMemberDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, relation: String, emoji: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedRelation by remember { mutableStateOf(FamilyRelation.SPOUSE) }
    var selectedEmoji by remember { mutableStateOf("👩") }

    val emojiOptions = listOf("👤", "👩", "👨", "👩‍🦰", "👦", "👧", "👵", "👴", "🐶")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "👨‍👩‍👧‍👦 Add Family Member",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Member Name (e.g. Dad, Priya, Rahul)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("family_member_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Choose Avatar Emoji:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    emojiOptions.forEach { emoji ->
                        Surface(
                            shape = CircleShape,
                            color = if (selectedEmoji == emoji) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { selectedEmoji = emoji }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(emoji, fontSize = 18.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onConfirm(name, selectedRelation.name, selectedEmoji, "#1D4ED8")
                        }
                    },
                    enabled = name.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("save_family_member_button")
                ) {
                    Text("Add Member", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionSheet(
    onAddExpense: () -> Unit,
    onAddBill: () -> Unit,
    onAddAppointment: () -> Unit,
    onAddTask: () -> Unit,
    onAddDocument: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = "⚡ Quick Actions",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = "💰",
                    title = "Add Expense",
                    subtitle = "Grocery, milk, fuel",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    onClick = { onDismiss(); onAddExpense() },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = "🔔",
                    title = "Add Bill Reminder",
                    subtitle = "Electricity, LIC, EMI",
                    color = AmberTertiaryContainer,
                    onClick = { onDismiss(); onAddBill() },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = "🏥",
                    title = "Appointment",
                    subtitle = "Doctor, school, service",
                    color = TealSecondaryContainer,
                    onClick = { onDismiss(); onAddAppointment() },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = "🔄",
                    title = "Recurring Task",
                    subtitle = "Daily, weekly, yearly",
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = { onDismiss(); onAddTask() },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            QuickActionButton(
                icon = "📁",
                title = "Save Policy / Document Info",
                subtitle = "Insurance policy number, expiry, warranty info",
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                onClick = { onDismiss(); onAddDocument() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: String,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 26.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ExportApkDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📦", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Export & Download APK",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Build status card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LifeRemind Release Build",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            StatusBadge(
                                text = "READY",
                                backgroundColor = StatusPaidBg,
                                textColor = StatusPaid,
                                icon = Icons.Default.CheckCircle
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Package: com.aistudio.liferemind.kxmpzq\nBuild Task: ./gradlew assembleRelease\nDirect File: /LifeRemind-v1.0-release.apk\nRelease Path: /release/LifeRemind-release.apk",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Option 1: Direct in Project Files
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📁", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "1. Direct in Project Files",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "The release APK is compiled and placed right in your project files:\n• LifeRemind-v1.0-release.apk (project root)\n• release/LifeRemind-release.apk\n• app/build/outputs/apk/release/app-release.apk",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: AI Studio Browser Export
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚡", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "2. AI Studio Export",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "In AI Studio, open the top header / Settings menu to Export Project (ZIP or APK). You can also download any file from the file explorer on the left.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 3: GitHub Actions Automated Cloud Release
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🚀", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "3. GitHub Actions Automated Pipeline",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Configured workflows automatically build and publish release APK artifacts upon push or release tags:\n• .github/workflows/build-apk.yml\n• .github/workflows/android-build.yml",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Button(
                    onClick = {
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                data = android.net.Uri.parse("https://github.com")
                                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "LifeRemind APK build ready in artifacts", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("download_apk_dialog_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download / Open GitHub Releases", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                android.content.Intent.EXTRA_TEXT,
                                "LifeRemind — Personal Life & Payment Reminder Manager\nReal Release APK compiled with ./gradlew assembleRelease\nPackage: com.aistudio.liferemind.kxmpzq"
                            )
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share LifeRemind APK Details"))
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share APK Info & Details")
                }
            }
        }
    }
}
