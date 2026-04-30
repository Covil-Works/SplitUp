package com.thaicrew.splitup.check.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thaicrew.splitup.check.domain.Check
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CheckScreen(
    viewModel: CheckViewModel,
    onNavigateToCreate: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val confirmationState by viewModel.confirmationState.collectAsStateWithLifecycle()

    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var checkName by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is CheckUiEvent.NavigateToCheckDetail -> {
                    onNavigateToCreate(event.checkId)
                }
            }
        }
    }

    // Dialog de Criação de Comanda
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(text = "Nova comanda") },
            text = {
                OutlinedTextField(
                    value = checkName,
                    onValueChange = { checkName = it },
                    singleLine = true,
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.onCreateCheckConfirmed(checkName)
                    showCreateDialog = false
                    checkName = ""
                }) { Text("Criar") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreateDialog = false
                        checkName = ""
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) { Text("Cancelar") }
            }
        )
    }

    // Dialog informativo (quando ação não pode ser executada)
    uiState.infoDialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.onDismissInfoDialog() },
            title = { Text(text = "Atenção") },
            text = { Text(text = message) },
            confirmButton = {
                Button(onClick = { viewModel.onDismissInfoDialog() }) { Text("Ok") }
            }
        )
    }

    // Dialog de Confirmação de Ação (Swipe)
    confirmationState?.let { (check, action) ->
        val title = if (action == CheckViewModel.SwipeAction.DELETE) "Excluir Comanda?" else "Fechar Comanda?"
        val message = if (action == CheckViewModel.SwipeAction.DELETE)
            "Tem certeza que deseja excluir '${check.name}'? Isso não pode ser desfeito."
        else
            "Deseja encerrar a comanda '${check.name}'?"
        val confirmText = if (action == CheckViewModel.SwipeAction.DELETE) "Excluir" else "Fechar"

        AlertDialog(
            onDismissRequest = { viewModel.onDismissDialog() },
            title = { Text(text = title) },
            text = { Text(text = message) },
            confirmButton = {
                Button(
                    onClick = { viewModel.onConfirmAction() } // O VM já sabe qual ação executar
                ) { Text(confirmText) }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onDismissDialog() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .size(72.dp),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Criar Comanda",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            CheckList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                checks = uiState.checks,
                onCheckClick = { id -> viewModel.onCheckClicked(id) },
                onSwipeAction = viewModel::onSwipeAction
            )
        }
    }
}

@Composable
private fun CheckList(
    modifier: Modifier = Modifier,
    checks: List<Check>,
    onCheckClick: (Int) -> Unit,
    onSwipeAction: (Check, CheckViewModel.SwipeAction) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 220.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "title") {
            Text(
                text = "Comandas",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (checks.isEmpty()) {
            item(key = "empty_state") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Nenhuma comanda aberta.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toque no + para adicionar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(checks, key = { it.id }) { check ->
                SwipeableCheckItem(
                    check = check,
                    onClick = { onCheckClick(check.id) },
                    onSwipeAction = onSwipeAction
                )
            }
        }
    }
}

@Composable
private fun SwipeableCheckItem(
    check: Check,
    onClick: () -> Unit,
    onSwipeAction: (Check, CheckViewModel.SwipeAction) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // Configuração do estado do Swipe
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSwipeAction(check, CheckViewModel.SwipeAction.CLOSE)
                    // Retornamos false para o item não sumir imediatamente antes do Dialog confirmar
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSwipeAction(check, CheckViewModel.SwipeAction.DELETE)
                    false
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
                else -> Color.Transparent
            }

            val alignment = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }

            val icon = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Check
                else -> null
            }

            val iconTint = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onError
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.onPrimary
                else -> MaterialTheme.colorScheme.onSurface
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint
                    )
                }
            }
        },
        content = {
            CheckListItem(check = check, onClick = onClick)
        }
    )
}

@Composable
private fun CheckListItem(check: Check, onClick: () -> Unit) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val dateString = dateFormatter.format(Date(check.creationDate))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            Text(text = check.name, style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Criada em: $dateString",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
