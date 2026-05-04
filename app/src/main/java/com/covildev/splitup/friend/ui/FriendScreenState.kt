package com.covildev.splitup.friend.ui

import com.covildev.splitup.friend.domain.Friend

/**
 * Representa todo o estado do ecrÃ£ de Amigos.
 * A UI observarÃ¡ este objeto para saber o que desenhar.
 * @param friends A lista atual de amigos a ser exibida.
 * @param isLoading Indica se os dados iniciais estÃ£o a ser carregados.
 * @param dialogState Indica o estado do pop up de desativaÃ§Ã£o do amigo
 */
data class FriendScreenState(
    val friends: List<Friend> = emptyList(),
    val isLoading: Boolean = true,
    val dialogState: DialogState = DialogState.Hidden
)

