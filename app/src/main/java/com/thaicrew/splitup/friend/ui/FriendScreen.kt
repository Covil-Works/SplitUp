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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
    // Recolhe o estado do ViewModel de forma segura em relação ao ciclo de vida
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostra a Snackbar quando houver uma mensagem de erro no estado
    LaunchedEffect(key1 = true) {
        viewModel.errorEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
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
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                FriendList(friends = uiState.friends)
            }
        }
    }
}

/**
 * Componente para a entrada de dados e botão de adicionar amigo.
 */
@Composable
private fun AddFriendInput(
    onAddFriend: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome do Amigo") },
            modifier = Modifier.weight(1f)
        )
        Button(onClick = {
            onAddFriend(name)
            name = "" // Limpa o campo após adicionar
        }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Amigo")
        }
    }
}

/**
 * Componente que exibe a lista de amigos.
 */
@Composable
private fun FriendList(
    friends: List<Friend>
) {
    if (friends.isEmpty()) {
        Text("Nenhum amigo adicionado ainda.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(friends, key = { friend -> friend.id }) { friend ->
                FriendListItem(friend = friend)
            }
        }
    }
}

/**
 * Componente que exibe um único item da lista de amigos.
 */
@Composable
private fun FriendListItem(
    friend: Friend
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = friend.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}