package com.thaicrew.splitup.friend.ui

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thaicrew.splitup.friend.domain.Friend

/**
 * O ecrã principal que conecta o ViewModel à UI.
 * Ele recolhe o estado e passa-o para os componentes de UI "burros".
 */
@Composable
fun FriendScreen(
    viewModel: FriendViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Mostra a Snackbar quando houver uma mensagem de erro no estado
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->

        when (val dialogState = uiState.dialogState) {
            is DialogState.ConfirmDeactivation -> {
                DeletionConfirmationDialog(
                    friendName = dialogState.friend.name,
                    onConfirm = { viewModel.onSoftDeleteConfirmed() },
                    onDismiss = { viewModel.onDialogDismiss() }
                )
            }
            DialogState.Hidden -> {
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AddFriendInput(
                onAddFriend = { name ->
                    viewModel.onAddFriend(name)
                },
                keyboardController = keyboardController,
                focusManager = focusManager
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                FriendList(
                    friends = uiState.friends,
                    onDeleteFriend = { friend -> viewModel.onSoftDeleteTriggered(friend)
                    }
                )
            }
        }
    }
}

/**
 * Componente para a entrada de dados e botão de adicionar amigo.
 */
@Composable
private fun AddFriendInput(
    onAddFriend: (String) -> Unit,
    keyboardController: SoftwareKeyboardController?,
    focusManager: FocusManager
) {
    var name by remember { mutableStateOf("") }
    val onAdd = {
        if (name.isNotBlank()) {
            keyboardController?.hide()
            focusManager.clearFocus()
            onAddFriend(name)
            name = ""
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome do Amigo") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onAdd() }
            )
        )
        Button(onClick = { onAdd() }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Amigo")
        }
    }
}

/**
 * Componente que exibe a lista de amigos.
 */
@Composable
private fun FriendList(
    friends: List<Friend>,
    onDeleteFriend: (Friend) -> Unit
) {
    if (friends.isEmpty()) {
        Text("Nenhum amigo adicionado ainda.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(friends, key = { friend -> friend.id }) { friend ->
                FriendListItem(
                    friend = friend,
                    onDeleteClick = onDeleteFriend
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
    onDeleteClick: (Friend) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp), // Ajuste no padding
            verticalAlignment = Alignment.CenterVertically, // Alinha verticalmente
            horizontalArrangement = Arrangement.SpaceBetween // Garante o espaçamento
        ) {
            Text(
                text = friend.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f) // Ocupa o espaço disponível
            )
            IconButton(onClick = { onDeleteClick(friend) }) { // Ação de clique
                Icon(
                    imageVector = Icons.Default.Delete, // Ícone de lixeira
                    contentDescription = "Deletar Amigo",
                    tint = MaterialTheme.colorScheme.error // Boa prática usar a cor de erro
                )
            }
        }
    }
}

@Composable
private fun DeletionConfirmationDialog(
    friendName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar exclusão?") },
        text = { Text("Você tem certeza que deseja desativar o amigo '$friendName'? Ele não poderá ser adicionado em novas comandas.") },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirmar")
            }
        }
    )
}