package com.thaicrew.splitup.history.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thaicrew.splitup.check.ui.CheckViewMode
import com.thaicrew.splitup.check.ui.CheckViewModeSelector
import com.thaicrew.splitup.check.ui.ExpandableItemCard
import com.thaicrew.splitup.check.ui.FriendOwedItem
import com.thaicrew.splitup.friend.domain.Friend
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Share

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaidCheckDetailScreen(
    viewModel: PaidCheckDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is PaidCheckUiEvent.NavigateBack -> onNavigateBack()
                is PaidCheckUiEvent.ShowSnackbar -> { /* Implementar Snackbar se tiver scaffold state */ }
                is PaidCheckUiEvent.ShareFile -> {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        event.file
                    )

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    context.startActivity(Intent.createChooser(intent, "Compartilhar Comanda"))
                }
            }
        }
    }

    // Dialogs
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDialogs() },
            title = { Text("Apagar Comanda") },
            text = { Text("Essa ação é irreversível.") },
            confirmButton = { Button(onClick = { viewModel.onDeleteConfirmed() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Apagar") } },
            dismissButton = { TextButton(onClick = { viewModel.onDismissDialogs() }) { Text("Cancelar") } }
        )
    }

    if (uiState.showReopenDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDialogs() },
            title = { Text("Reabrir Comanda") },
            text = { Text("A comanda voltará para a lista de abertas.") },
            confirmButton = { Button(onClick = { viewModel.onReopenConfirmed() }) { Text("Reabrir") } },
            dismissButton = { TextButton(onClick = { viewModel.onDismissDialogs() }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = uiState.check?.name ?: "Detalhes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onExportClicked() }) {
                        Icon(Icons.Default.Share, contentDescription = "Exportar PDF")
                    }

                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opções")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Reabrir Comanda") },
                            onClick = { showMenu = false; viewModel.onReopenClicked() }
                        )
                        DropdownMenuItem(
                            text = { Text("Apagar Comanda", color = MaterialTheme.colorScheme.error) },
                            onClick = { showMenu = false; viewModel.onDeleteClicked() }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

                // 1. Cabeçalho de Total
                val totalFormatted = String.format("%.2f", uiState.checkTotal / 100.0)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total Pago", style = MaterialTheme.typography.labelMedium)
                        Text("R$ $totalFormatted", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                }

                // 2. Seletor de Modo
                CheckViewModeSelector(
                    currentMode = uiState.viewMode,
                    onModeSelected = { viewModel.onChangeViewMode(it) }
                )

                // 3. Listas
                LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                    when (uiState.viewMode) {
                        CheckViewMode.ByItem -> {
                            items(uiState.itemsWithSharers) { itemWithSharers ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                    ExpandableItemCard(
                                        itemWithSharers = itemWithSharers,
                                        isExpanded = uiState.expandedItemId == itemWithSharers.item.id,
                                        isEditing = false, // TRAVA VISUAL: Nunca entra em modo edição
                                        editingName = "", editingQuantity = 0, editingValue = "", editingSharers = emptySet(),
                                        allParticipants = emptyList(),
                                        onClickExpand = { viewModel.onExpandItem(itemWithSharers.item.id) },
                                        onStartEdit = {}, onCancelEdit = {}, onNameChange = {}, onQuantityChange = {}, onValueChange = {}, onToggleFriend = {}, onSaveClick = {}, onDeleteClick = {}
                                    )
                                }
                            }
                        }
                        CheckViewMode.ByFriend -> {
                            items(uiState.participants) { friend ->
                                val totalOwed = uiState.friendTotals[friend.id] ?: 0L
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                    ExpandableFriendTotalCard(
                                        friend = friend,
                                        totalInCents = totalOwed,
                                        isExpanded = uiState.expandedFriendId == friend.id,
                                        owedItems = if(uiState.expandedFriendId == friend.id) uiState.expandedFriendOwedItems else emptyList(),
                                        onClickExpand = { viewModel.onFriendTotalClicked(friend.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableFriendTotalCard(
    friend: Friend,
    totalInCents: Long,
    isExpanded: Boolean,
    owedItems: List<FriendOwedItem>,
    onClickExpand: () -> Unit
) {
    val totalFormatted = String.format("%.2f", totalInCents / 100.0)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 4.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onClickExpand() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabeçalho (sempre visível)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = friend.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (totalInCents == 0L) "Nada a pagar" else "Paga a sua parte",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "R$ $totalFormatted",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Detalhes (só quando expandido)
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                if (owedItems.isEmpty()) {
                    Text(
                        text = "Nenhum item para mostrar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    owedItems.forEach { owed ->
                        val amountFormatted = String.format("%.2f", owed.amountInCents / 100.0)
                        val unitFormatted = String.format("%.2f", owed.unitValueInCents / 100.0)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = owed.itemName, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "${owed.quantity}x × R$ $unitFormatted",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "R$ $amountFormatted",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
