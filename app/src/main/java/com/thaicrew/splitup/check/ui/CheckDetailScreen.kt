package com.thaicrew.splitup.check.ui

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
import timber.log.Timber
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.mutableStateListOf

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckDetailScreen(
    viewModel: CheckDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is CheckDetailUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                CheckDetailUiEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    if (showEditDialog && uiState.check != null) {
        EditCheckNameDialog(
            currentName = uiState.check!!.name,
            onConfirm = { newName ->
                viewModel.onNameChanged(newName)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    // Controle do Bottom Sheet
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
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar nome",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.isError) {
                Text("Erro ao carregar", modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    ParticipantsSection(
                        participants = uiState.participants,
                        onAddClicked = { viewModel.onAddFriendsClicked() },
                        onRemoveParticipant = { id -> viewModel.onRemoveParticipant(id) }
                    )

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Seção de Itens em breve...")
                    }
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