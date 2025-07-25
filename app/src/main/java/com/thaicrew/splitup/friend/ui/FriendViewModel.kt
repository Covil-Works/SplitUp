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
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

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
    // UseCases serão adicionados aqui.

) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendScreenState())
    val uiState: StateFlow<FriendScreenState> = _uiState.asStateFlow()
    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()


    init {
        observeFriends()
    }

    private fun observeFriends() {
        getActiveFriendsUseCase()
            .onEach { friends ->
                _uiState.update { currentState ->
                    currentState.copy(friends = friends, isLoading = false)
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Função chamada pela UI para tratar do evento de adicionar um novo amigo.
     */
    fun onAddFriend(name: String) {
        viewModelScope.launch {
            try {
                addFriendUseCase(name)
                _uiState.update { it.copy(errorMessage = null) }
            } catch (e: InvalidFriendNameException) {
                _errorEvent.emit(e.message ?: "Nome inválido")
            } catch (e: FriendAlreadyExistsException) {
                _errorEvent.emit(e.message ?: "Amigo já existe")
            }
        }
    }
}