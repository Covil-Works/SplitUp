package com.thaicrew.splitup.friend.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thaicrew.splitup.friend.domain.AddFriendUseCase
import com.thaicrew.splitup.friend.domain.Friend
import com.thaicrew.splitup.friend.domain.FriendAlreadyExistsException
import com.thaicrew.splitup.friend.domain.GetActiveFriendsUseCase
import com.thaicrew.splitup.friend.domain.InvalidFriendNameException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Representa todo o estado do ecrã de Amigos.
 * A UI observará este objeto para saber o que desenhar.
 * @param friends A lista atual de amigos a ser exibida.
 * @param isLoading Indica se os dados iniciais estão a ser carregados.
 * @param errorMessage Uma mensagem de erro a ser exibida, se houver.
 */
data class FriendScreenState(
    val friends: List<Friend> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val getActiveFriendsUseCase: GetActiveFriendsUseCase,
    private val addFriendUseCase: AddFriendUseCase
    // Mais UseCases (update, delete) seriam adicionados aqui.
) : ViewModel() {

    // O guardião interno e mutável do estado.
    private val _uiState = MutableStateFlow(FriendScreenState())
    // A versão pública e somente de leitura que a UI vai observar.
    val uiState: StateFlow<FriendScreenState> = _uiState.asStateFlow()

    init {
        // Assim que o ViewModel é criado, começa a observar as mudanças na lista de amigos.
        observeFriends()
    }

    private fun observeFriends() {
        getActiveFriendsUseCase() // Chama o UseCase, que retorna um Flow
            .onEach { friends ->
                // Sempre que uma nova lista de amigos chega do Flow...
                _uiState.update { currentState ->
                    // ...atualizamos o nosso estado com a nova lista.
                    currentState.copy(friends = friends, isLoading = false)
                }
            }
            .launchIn(viewModelScope) // Inicia a observação no escopo do ViewModel.
    }

    /**
     * Função chamada pela UI para tratar do evento de adicionar um novo amigo.
     */
    fun onAddFriend(name: String) {
        viewModelScope.launch {
            try {
                // Tenta executar o caso de uso.
                addFriendUseCase(name)
                // Se for bem-sucedido, limpa qualquer mensagem de erro antiga.
                _uiState.update { it.copy(errorMessage = null) }
            } catch (e: InvalidFriendNameException) {
                // Se apanhar uma exceção de negócio, atualiza o estado com a mensagem de erro.
                _uiState.update { it.copy(errorMessage = e.message) }
            } catch (e: FriendAlreadyExistsException) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }
}