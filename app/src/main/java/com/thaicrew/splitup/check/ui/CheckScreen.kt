package com.thaicrew.splitup.check.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thaicrew.splitup.Screen
import com.thaicrew.splitup.check.domain.Check
import com.thaicrew.splitup.ui.theme.LightGreyText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CheckScreen(
    viewModel: CheckViewModel,
    onNavigateToCreate: (Int) -> Unit
) {
    val fabInteractionSource = remember { MutableInteractionSource() }
    val isFabPressed by fabInteractionSource.collectIsPressedAsState()
    val fabScale by animateFloatAsState(
        targetValue = if (isFabPressed) 1.08f else 1f,
        label = "check_fab_scale"
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val confirmationState by viewModel.confirmationState.collectAsStateWithLifecycle()

    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var checkName by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is CheckUiEvent.NavigateToCheckDetail -> onNavigateToCreate(event.checkId)
            }
        }
    }

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

    uiState.infoDialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.onDismissInfoDialog() },
            title = { Text(text = "Atencao") },
            text = { Text(text = message) },
            confirmButton = {
                Button(onClick = { viewModel.onDismissInfoDialog() }) { Text("Ok") }
            }
        )
    }

    confirmationState?.let { (check, action) ->
        val title = if (action == CheckViewModel.SwipeAction.DELETE) "Excluir Comanda?" else "Fechar Comanda?"
        val message = if (action == CheckViewModel.SwipeAction.DELETE) {
            "Tem certeza que deseja excluir '${check.name}'? Isso nao pode ser desfeito."
        } else {
            "Deseja encerrar a comanda '${check.name}'?"
        }
        val confirmText = if (action == CheckViewModel.SwipeAction.DELETE) "Excluir" else "Fechar"

        AlertDialog(
            onDismissRequest = { viewModel.onDismissDialog() },
            title = { Text(text = title) },
            text = { Text(text = message) },
            confirmButton = {
                Button(onClick = { viewModel.onConfirmAction() }) { Text(confirmText) }
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
                    .scale(fabScale)
                    .size(64.dp),
                interactionSource = fabInteractionSource,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Criar Comanda",
                    modifier = Modifier.size(30.dp)
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
                participantsByCheckId = uiState.participantsByCheckId,
                totalByCheckId = uiState.totalByCheckId,
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
    participantsByCheckId: Map<Int, List<String>>,
    totalByCheckId: Map<Int, Long>,
    onCheckClick: (Int) -> Unit,
    onSwipeAction: (Check, CheckViewModel.SwipeAction) -> Unit
) {
    var expandedCheckId by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "title") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Screen.Checks.icon,
                    contentDescription = Screen.Checks.label,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = Screen.Checks.label,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
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
                    isExpanded = expandedCheckId == check.id,
                    participants = participantsByCheckId[check.id].orEmpty(),
                    totalInCents = totalByCheckId[check.id] ?: 0L,
                    onClick = { onCheckClick(check.id) },
                    onMoreClick = {
                        expandedCheckId = if (expandedCheckId == check.id) null else check.id
                    },
                    onSwipeAction = onSwipeAction
                )
            }
        }
    }
}

@Composable
private fun SwipeableCheckItem(
    check: Check,
    isExpanded: Boolean,
    participants: List<String>,
    totalInCents: Long,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    onSwipeAction: (Check, CheckViewModel.SwipeAction) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val cardShape = MaterialTheme.shapes.medium

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSwipeAction(check, CheckViewModel.SwipeAction.CLOSE)
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
        modifier = Modifier.clip(cardShape),
        state = dismissState,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
                else -> Color.Transparent
            }

            val alignment = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }

            val icon = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Check
                else -> null
            }

            val iconTint = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onError
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.onPrimary
                else -> MaterialTheme.colorScheme.onSurface
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = color, shape = cardShape)
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
            CheckListItem(
                check = check,
                isExpanded = isExpanded,
                participants = participants,
                totalInCents = totalInCents,
                shape = cardShape,
                onCardClick = onClick,
                onMoreClick = onMoreClick,
                onCloseClick = { onSwipeAction(check, CheckViewModel.SwipeAction.CLOSE) },
                onDeleteClick = { onSwipeAction(check, CheckViewModel.SwipeAction.DELETE) }
            )
        }
    )
}

@Composable
private fun CheckListItem(
    check: Check,
    isExpanded: Boolean,
    participants: List<String>,
    totalInCents: Long,
    shape: Shape,
    onCardClick: () -> Unit,
    onMoreClick: () -> Unit,
    onCloseClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val dateString = dateFormatter.format(Date(check.creationDate))
    val totalFormatted = String.format(Locale.US, "%.2f", totalInCents / 100.0)
    val participantsText = if (participants.isEmpty()) {
        "nenhum"
    } else {
        participants.joinToString(separator = ", ")
    }

    Card(
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = check.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = LightGreyText,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Expandir comanda",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(onClick = onMoreClick)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Data de criacao",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Participantes da comanda",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = participantsText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Total ate agora: R$ $totalFormatted",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = onCloseClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Fechar comanda"
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Fechar")
                        }

                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = onDeleteClick
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Apagar comanda",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "Apagar",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
