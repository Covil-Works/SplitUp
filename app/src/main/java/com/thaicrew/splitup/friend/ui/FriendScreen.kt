package com.thaicrew.splitup.friend.ui

import android.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thaicrew.splitup.friend.domain.Friend
import timber.log.Timber

/**
 * O ecrã principal que conecta o ViewModel à UI.
 */
@Composable
fun FriendScreen(
    viewModel: FriendViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    Timber.d("FriendScreen em recomposição. DialogState: ${uiState.dialogState::class.simpleName}, Loading: ${uiState.isLoading}")

    // Mostra a Snackbar quando houver uma mensagem de erro no estado
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    Timber.i("Recebido UiEvent para mostrar Snackbar com a mensagem: '${event.message}'")
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    // Lógica para mostrar o Dialog de Adição
    if (showAddFriendDialog) {
        AddFriendDialog(
            onDismiss = { showAddFriendDialog = false },
            onConfirm = { name ->
                viewModel.onAddFriend(name)
                showAddFriendDialog = false
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddFriendDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Amigo")
            }
        }
    ) { paddingValues ->

        // Gerenciamento dos Dialogs globais (Deleção, Edição, Reativação) vindos do ViewModel
        when (val dialogState = uiState.dialogState) {
            is DialogState.ConfirmDeactivation -> {
                DeletionConfirmationDialog(
                    friendName = dialogState.friend.name,
                    onConfirm = {
                        viewModel.onSoftDeleteConfirmed()
                    },
                    onDismiss = {
                        viewModel.onDialogDismiss()
                    }
                )
            }
            is DialogState.ConfirmReactivation -> {
                ReactivationConfirmationDialog(
                    friendName = dialogState.friend.name,
                    onConfirm = {
                        viewModel.onReactivationConfirmed()
                    },
                    onDismiss = {
                        viewModel.onDialogDismiss()
                    }
                )
            }
            is DialogState.ShowEdit -> {
                EditFriendDialog(
                    friend = dialogState.friend,
                    onConfirm = { newName ->
                        viewModel.onEditConfirmed(dialogState.friend, newName)
                    },
                    onDismiss = {
                        viewModel.onDialogDismiss()
                    }
                )
            }
            is DialogState.CannotDelete -> {
                CannotDeleteDialog(
                    friendName = dialogState.friend.name,
                    checkNames = dialogState.checkNames,
                    onDismiss = { viewModel.onDialogDismiss() }
                )
            }
            is DialogState.ConfirmHardDelete -> {
                HardDeleteConfirmationDialog(
                    friendName = dialogState.friend.name,
                    onConfirm = { viewModel.onHardDeleteConfirmed() },
                    onDismiss = { viewModel.onDialogDismiss() }
                )
            }
            DialogState.Hidden -> {
                // Nenhum log necessário quando o diálogo está oculto.
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                FriendList(
                    friends = uiState.friends,
                    onDeleteFriend = { friend ->
                        Timber.d("Botão 'Delete' clicado para o amigo: '${friend.name}' (ID: ${friend.id})")
                        viewModel.onDeleteTriggered(friend)
                    },
                    onEditFriend = { friend ->
                        Timber.d("Botão 'Edit' clicado para o amigo: '${friend.name}' (ID: ${friend.id})")
                        viewModel.onEditTriggered(friend)
                    }
                )
            }
        }
    }
}

/**
 * Dialog específico para adicionar um novo amigo.
 * Segue o padrão visual do CheckScreen, mas abstraído em um componente para limpeza.
 */
@Composable
private fun AddFriendDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Novo amigo") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        // Enter apenas fecha o teclado
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                )
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.secondary)
            ){
                Text("Cancelar")
            }
        }
    )
}

/**
 * Componente que exibe a lista de amigos.
 */
@Composable
private fun FriendList(
    friends: List<Friend>,
    onDeleteFriend: (Friend) -> Unit,
    onEditFriend: (Friend) -> Unit
) {
    if (friends.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Nenhum amigo ativo no momento.")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Toque no + para adicionar",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(friends, key = { friend -> friend.id }) { friend ->
                FriendListItem(
                    friend = friend,
                    onDeleteClick = onDeleteFriend,
                    onEditClick = onEditFriend
                )
            }
        }
    }
}

/**
 * Componente que exibe um único item da lista de amigos.
 */
@Composable
private fun FriendListItem(
    friend: Friend,
    onDeleteClick: (Friend) -> Unit,
    onEditClick: (Friend) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = friend.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onEditClick(friend) }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar Amigo",
                )
            }
            IconButton(onClick = { onDeleteClick(friend) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Deletar Amigo",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// --- DIÁLOGOS DE CONFIRMAÇÃO DO VIEWMODEL (Mantidos como estavam) ---

@Composable
private fun DeletionConfirmationDialog(
    friendName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar exclusão?") },
        text = { Text("Você tem certeza que deseja apagar $friendName? Esse amigo não poderá ser adicionado em novas comandas.") },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.secondary)
            ){
                Text("Cancelar")
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Apagar")
            }
        }
    )
}

@Composable
private fun ReactivationConfirmationDialog(
    friendName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar reativação?") },
        text = { Text("O amigo '$friendName' já existe mas está desativado. Deseja reativá-lo?") },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Não")
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Sim, reativar")
            }
        }
    )
}

@Composable
private fun EditFriendDialog(
    friend: Friend,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(friend.name) }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar amigo") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome do Amigo") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                })
            )
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Salvar")
            }
        }
    )
}

@Composable
private fun CannotDeleteDialog(
    friendName: String,
    checkNames: List<String>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Não é possível remover") },
        text = {
            Column {
                Text("O amigo '$friendName' está nas seguintes comandas abertas:")
                Spacer(modifier = Modifier.height(8.dp))
                checkNames.forEach { name ->
                    Text("• $name", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Remova-o das comandas antes de excluir.")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            )
            { Text("Entendi") }
        }
    )
}

@Composable
private fun HardDeleteConfirmationDialog(
    friendName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir permanentemente?") },
        text = { Text("Você tem certeza que deseja apagar $friendName? Esse amigo não poderá ser adicionado em novas comandas") },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.secondary)
            )
            {
                Text("Cancelar")
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Apagar")
            }
        }
    )
}