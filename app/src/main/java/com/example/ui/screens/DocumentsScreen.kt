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
fun DocumentsScreen(
    documents: List<DocumentEntity>,
    familyMembers: List<FamilyMemberEntity>,
    selectedFamily: String,
    onFamilySelect: (String) -> Unit,
    onDeleteDocument: (DocumentEntity) -> Unit,
    onAddDocumentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTypeFilter by remember { mutableStateOf("ALL") }

    val familyFiltered = if (selectedFamily == "ALL") documents else documents.filter {
        it.familyMember.equals(selectedFamily, ignoreCase = true) || it.familyMember == "Family"
    }

    val filteredDocs = if (selectedTypeFilter == "ALL") familyFiltered else familyFiltered.filter {
        it.docType.equals(selectedTypeFilter, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddDocumentClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 70.dp).testTag("add_doc_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Document")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("documents_screen_list"),
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

            // Doc Type Filter Chips
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
                        selected = selectedTypeFilter == "ALL",
                        onClick = { selectedTypeFilter = "ALL" },
                        label = { Text("All Vault Items") }
                    )
                    DocType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedTypeFilter == type.name,
                            onClick = { selectedTypeFilter = type.name },
                            label = { Text("${type.iconEmoji} ${type.displayName}") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (filteredDocs.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = "📁",
                        title = "No Documents Saved",
                        description = "Store insurance policy numbers, vehicle registration cards, property tax records, and appliance warranty details safely on-device.",
                        actionButtonText = "Add Policy / Document",
                        onActionClick = onAddDocumentClick
                    )
                }
            } else {
                items(filteredDocs, key = { it.id }) { doc ->
                    DocumentCardItem(
                        doc = doc,
                        onDelete = { onDeleteDocument(doc) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentCardItem(
    doc: DocumentEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val docType = DocType.fromString(doc.docType)

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
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(docType.iconEmoji, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = doc.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (doc.issuerOrProvider.isNotBlank()) {
                            Text(
                                text = "Provider: ${doc.issuerOrProvider}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }

                if (doc.expiryDateMillis != null) {
                    StatusBadge(
                        text = "Exp: ${DateTimeUtils.formatDate(doc.expiryDateMillis)}",
                        backgroundColor = AmberTertiaryContainer,
                        textColor = AmberTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (doc.identifierOrPolicyNo.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Policy / ID: ${doc.identifierOrPolicyNo}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (doc.notes.isNotBlank()) {
                Text(
                    text = doc.notes,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Family: ${doc.familyMember}",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
