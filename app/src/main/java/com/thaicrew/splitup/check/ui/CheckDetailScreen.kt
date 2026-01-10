package com.thaicrew.splitup.check.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckDetailScreen(
    viewModel: CheckDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showEditDialog by remember { mutableStateOf(false) }

    if (uiState.showCloseCheckDialog) {
        CloseCheckConfirmationDialog(
            onConfirm = { viewModel.onCloseCheckConfirmed() },
            onDismiss = { viewModel.onDismissCloseCheckDialog() }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is CheckDetailUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                CheckDetailUiEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    if (showEditDialog && uiState.check != null) {
        EditCheckNameDialog(
            currentName = uiState.check!!.name,
            onConfirm = { newName -> viewModel.onNameChanged(newName); showEditDialog = false },
            onDismiss = { showEditDialog = false }
        )
    }

    if (uiState.showBottomSheet) {
        SelectFriendsBottomSheet(
            availableFriends = uiState.availableFriends,
            onDismiss = { viewModel.onDismissBottomSheet() },
            onConfirm = { ids -> viewModel.onConfirmParticipants(ids) }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showEditDialog = true }
                    ) {
                        Text(text = uiState.check?.name ?: "Carregando...")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            // AQUI ESTÁ A MÁGICA: Uma única LazyColumn para a tela toda
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 80.dp) // Espaço extra no fim
            ) {
                // --- SEÇÃO 1: Participantes (Topo) ---
                item {
                    ParticipantsSection(
                        participants = uiState.participants,
                        onAddClicked = { viewModel.onAddFriendsClicked() },
                        onRemoveParticipant = { id -> viewModel.onRemoveParticipant(id) }
                    )
                    Divider()
                }

                // --- SEÇÃO 2: Adicionar Item (Formulário) ---
                item {
                    AddItemSection(
                        name = uiState.newItemName,
                        quantity = uiState.newItemQuantity,
                        value = uiState.newItemValue,
                        onNameChange = { viewModel.onNewItemNameChanged(it) },
                        onQuantityChange = { viewModel.onNewItemQuantityChanged(it) },
                        onValueChange = { viewModel.onNewItemValueChanged(it) }
                    )
                }

                // --- SEÇÃO 3: Quem divide o item ---
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    ItemParticipantsSection(
                        allParticipants = uiState.participants,
                        selectedIds = uiState.selectedFriendIdsForItem,
                        isAllSelected = uiState.isAllSelected,
                        onToggleFriend = { viewModel.onToggleFriendSelection(it) },
                        onRemoveAll = { viewModel.onRemoveAllSelection() }
                    )
                }

                // --- SEÇÃO 4: Botão de Ação ---
                item {
                    val hasParticipants = uiState.participants.isNotEmpty()
                    val isSelectionValid = (uiState.isAllSelected && hasParticipants) || uiState.selectedFriendIdsForItem.isNotEmpty()
                    val isFormValid = uiState.newItemName.isNotBlank() && uiState.newItemValue.isNotBlank() && isSelectionValid

                    AddItemButton(
                        isEnabled = isFormValid,
                        onAddClicked = { viewModel.onAddItemClicked() }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }

                // --- SEÇÃO 5: Seletor de Visualização ---
                item {
                    CheckViewModeSelector(
                        currentMode = uiState.viewMode,
                        onModeSelected = { viewModel.onChangeViewMode(it) }
                    )
                }

                // --- SEÇÃO 6: Lista Dinâmica (Muda conforme a aba) ---
                when (uiState.viewMode) {
                    CheckViewMode.ByFriend -> {
                        if (uiState.participants.isEmpty()) {
                            item {
                                Text(
                                    "Adicione amigos para ver a divisão.",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            items(uiState.participants) { friend ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                    FriendTotalCard(
                                        friend = friend,
                                        totalInCents = uiState.friendTotals[friend.id] ?: 0L
                                    )
                                }
                            }
                        }
                    }
                    CheckViewMode.ByItem -> {
                        items(uiState.itemsWithSharers) { itemWithSharers ->
                            val isExpanded = uiState.editingItemId == itemWithSharers.item.id

                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                ExpandableItemCard(
                                    itemWithSharers = itemWithSharers,
                                    isExpanded = isExpanded,
                                    editingName = uiState.editingName,
                                    editingQuantity = uiState.editingQuantity,
                                    editingValue = uiState.editingValue,
                                    editingSharers = uiState.editingSharers,
                                    allParticipants = uiState.participants,
                                    onClick = {
                                        if (isExpanded) viewModel.onCollapseItem()
                                        else viewModel.onExpandItem(itemWithSharers)
                                    },
                                    onNameChange = { viewModel.onEditNameChange(it) },
                                    onQuantityChange = { viewModel.onEditQuantityChange(it) },
                                    onValueChange = { viewModel.onEditValueChange(it) },
                                    onToggleFriend = { viewModel.onEditToggleFriend(it) },
                                    onSaveClick = { viewModel.onSaveEditClicked() },
                                    onDeleteClick = { viewModel.onDeleteEditClicked() }
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Ações da Comanda",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    CloseCheckButton(
                        onClick = { viewModel.onCloseCheckClicked() }
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectFriendsBottomSheet(
    availableFriends: List<com.thaicrew.splitup.friend.domain.Friend>,
    onDismiss: () -> Unit,
    onConfirm: (List<Int>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    // Lista temporária para guardar os IDs selecionados no sheet
    val selectedIds = remember { mutableStateListOf<Int>() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = "Selecionar amigos",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false) // Não ocupa a tela toda se tiver poucos amigos
            ) {
                items(availableFriends) { friend ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedIds.contains(friend.id)) {
                                    selectedIds.remove(friend.id)
                                } else {
                                    selectedIds.add(friend.id)
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = friend.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Checkbox(
                            checked = selectedIds.contains(friend.id),
                            onCheckedChange = null // O clique é tratado na Row para melhor UX
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onConfirm(selectedIds.toList()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedIds.isNotEmpty()
            ) {
                Text("Adicionar selecionados")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParticipantsSection(
    participants: List<com.thaicrew.splitup.friend.domain.Friend>,
    onAddClicked: () -> Unit,
    onRemoveParticipant: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Amigos",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (participants.isEmpty()) {
            // Estado Inicial: Vazio
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Adicione os amigos que participam dessa comanda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onAddClicked) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adicionar amigos")
                }
            }
        } else {
            // Estado: Com Participantes
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                participants.forEach { friend ->
                    InputChip(
                        selected = false,
                        onClick = { /* Ação futura: Ver gastos do amigo */ },
                        label = { Text(friend.name) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remover",
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { onRemoveParticipant(friend.id) }
                            )
                        }
                    )
                }

                // Botão "+" ao final da lista
                IconButton(
                    onClick = onAddClicked,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adicionar mais",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun EditCheckNameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar nome da comanda") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddItemSection(
    name: String,
    quantity: Int,
    value: String,
    onNameChange: (String) -> Unit,
    onQuantityChange: (Int) -> Unit,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Novo Item",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 1. Campo Nome do Item
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nome do item (ex: Cerveja)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Linha com Quantidade e Valor
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Controle de Quantidade
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.small
                    )
            ) {
                IconButton(onClick = { onQuantityChange(-1) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Diminuir")
                }
                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = { onQuantityChange(1) }) {
                    Icon(Icons.Default.Add, contentDescription = "Aumentar")
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Campo de preço com teclado numérico
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text("Valor total") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                prefix = { Text("R$ ") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ItemParticipantsSection(
    allParticipants: List<com.thaicrew.splitup.friend.domain.Friend>,
    selectedIds: Set<Int>,
    isAllSelected: Boolean, // Novo parâmetro
    onToggleFriend: (Int) -> Unit,
    onRemoveAll: () -> Unit // Nova callback
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Quem divide?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (allParticipants.isEmpty()) {
            Text(
                text = "Adicione amigos à comanda primeiro.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            // Lógica de Exibição: TODOS ou MANUAL
            if (isAllSelected) {
                // Modo Automático
                InputChip(
                    selected = true,
                    onClick = { /* Não faz nada ao clicar no corpo, apenas no X */ },
                    label = { Text("TODOS") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remover Todos",
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onRemoveAll() }
                        )
                    }
                )
            } else {
                // Modo Manual
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allParticipants.forEach { friend ->
                        FilterChip(
                            selected = selectedIds.contains(friend.id),
                            onClick = { onToggleFriend(friend.id) },
                            label = { Text(friend.name) },
                            leadingIcon = if (selectedIds.contains(friend.id)) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selecionado",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddItemButton(
    isEnabled: Boolean,
    onAddClicked: () -> Unit
) {
    Button(
        onClick = onAddClicked,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Adicionar Item")
    }
}

@Composable
fun CheckViewModeSelector(
    currentMode: CheckViewMode,
    onModeSelected: (CheckViewMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(48.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraLarge),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Opção: POR ITEM
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(4.dp)
                .clip(MaterialTheme.shapes.large)
                .background(if (currentMode == CheckViewMode.ByItem) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { onModeSelected(CheckViewMode.ByItem) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Por Item",
                color = if (currentMode == CheckViewMode.ByItem) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Opção: POR PESSOA
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(4.dp)
                .clip(MaterialTheme.shapes.large)
                .background(if (currentMode == CheckViewMode.ByFriend) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { onModeSelected(CheckViewMode.ByFriend) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Por Pessoa",
                color = if (currentMode == CheckViewMode.ByFriend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun FriendTotalsList(
    participants: List<com.thaicrew.splitup.friend.domain.Friend>,
    totals: Map<Int, Long>
) {
    if (participants.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("Nenhum amigo na comanda ainda.", color = MaterialTheme.colorScheme.secondary)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(participants) { friend ->
                val totalInCents = totals[friend.id] ?: 0L
                val totalFormatted = String.format("%.2f", totalInCents / 100.0)

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = friend.name,
                                style = MaterialTheme.typography.titleMedium
                            )
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
                }
            }
        }
    }
}

@Composable
fun FriendTotalCard(
    friend: com.thaicrew.splitup.friend.domain.Friend,
    totalInCents: Long
) {
    val totalFormatted = String.format("%.2f", totalInCents / 100.0)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
    }
}

@Composable
fun ItemSummaryCard(
    item: com.thaicrew.splitup.check.domain.Item
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${item.quantity}x",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Text(
                text = "R$ ${String.format("%.2f", item.valueInCents / 100.0)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpandableItemCard(
    itemWithSharers: com.thaicrew.splitup.check.domain.ItemWithSharers,
    isExpanded: Boolean,
    // Dados de Edição
    editingName: String,
    editingQuantity: Int,
    editingValue: String,
    editingSharers: Set<Int>,
    allParticipants: List<com.thaicrew.splitup.friend.domain.Friend>,
    // Eventos
    onClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onQuantityChange: (Int) -> Unit,
    onValueChange: (String) -> Unit,
    onToggleFriend: (Int) -> Unit,
    onSaveClick: () -> Unit,   // Implementaremos no próximo passo
    onDeleteClick: () -> Unit  // Implementaremos no próximo passo
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { if (!isExpanded) onClick() }, // Só clica para abrir se estiver fechado
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            if (isExpanded) {
                // --- MODO EDIÇÃO ---
                Text(
                    "Editar Item",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Reutilizando os componentes que já criamos!
                // Cuidado: AddItemSection e ItemParticipantsSection precisam ser acessíveis aqui.

                // Input Nome
                OutlinedTextField(
                    value = editingName,
                    onValueChange = onNameChange,
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Input Qtd e Valor (Replicando layout simplificado ou reusando)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Botões de Qtd
                    IconButton(onClick = { onQuantityChange(-1) }) { Icon(Icons.Default.Remove, null) }
                    Text("$editingQuantity", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { onQuantityChange(1) }) { Icon(Icons.Default.Add, null) }

                    Spacer(modifier = Modifier.width(16.dp))

                    OutlinedTextField(
                        value = editingValue,
                        onValueChange = onValueChange,
                        label = { Text("Centavos") }, // Ajustaremos formatação visual depois
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quem Divide (Modo Manual Simplificado para Edição)
                Text("Quem divide agora:", style = MaterialTheme.typography.bodySmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    allParticipants.forEach { friend ->
                        FilterChip(
                            selected = editingSharers.contains(friend.id),
                            onClick = { onToggleFriend(friend.id) },
                            label = { Text(friend.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botões de Ação (Salvar / Excluir / Fechar)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDeleteClick) {
                        Text("Excluir", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onClick) { // Reutiliza onClick para fechar/cancelar
                        Text("Cancelar")
                    }
                    Button(onClick = onSaveClick) {
                        Text("Salvar")
                    }
                }

            } else {
                // --- MODO RESUMO (O que já tínhamos) ---
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = itemWithSharers.item.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "${itemWithSharers.item.quantity}x • ${itemWithSharers.sharersIds.size} dividindo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Text(
                        text = "R$ ${String.format("%.2f", itemWithSharers.item.valueInCents / 100.0)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CloseCheckButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error // Vermelho para indicar ação destrutiva/final
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Fechar Comanda")
    }
}

@Composable
fun CloseCheckConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fechar comanda?") },
        text = { Text("Ao fechar a comanda, ela será marcada como paga e arquivada. Tem certeza?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Sim, fechar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}