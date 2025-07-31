package com.thaicrew.splitup.friend.ui

import com.thaicrew.splitup.friend.domain.Friend

/**
 * Representa todo o estado do ecrã de Amigos.
 * A UI observará este objeto para saber o que desenhar.
 * @param friends A lista atual de amigos a ser exibida.
 * @param isLoading Indica se os dados iniciais estão a ser carregados.
 * @param dialogState Indica o estado do pop up de desativação do amigo
 */
data class FriendScreenState(
    val friends: List<Friend> = emptyList(),
    val isLoading: Boolean = true,
    val dialogState: DialogState = DialogState.Hidden
)
