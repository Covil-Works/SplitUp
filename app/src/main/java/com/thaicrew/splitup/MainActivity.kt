package com.thaicrew.splitup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.thaicrew.splitup.common.data.local.AppDatabase
import com.thaicrew.splitup.friend.data.FriendRepositoryImpl
import com.thaicrew.splitup.friend.domain.AddFriendUseCase
import com.thaicrew.splitup.friend.domain.GetActiveFriendsUseCase
import com.thaicrew.splitup.friend.ui.FriendScreen
import com.thaicrew.splitup.friend.ui.FriendViewModel
import com.thaicrew.splitup.ui.theme.SplitUpTheme

class MainActivity : ComponentActivity() {

    // Vamos construir a nossa cadeia de dependências manualmente aqui.
    // Mais tarde, um framework de Injeção de Dependência (Hilt) fará isto por nós.
    // Dentro de MainActivity.kt
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "splitup.db"
        )
            .allowMainThreadQueries() // <-- ADICIONE ESTA LINHA
            .build()
    }

    private val friendDao by lazy {
        database.friendDao()
    }

    private val friendRepository by lazy {
        FriendRepositoryImpl(friendDao)
    }

    private val getActiveFriendsUseCase by lazy {
        GetActiveFriendsUseCase(friendRepository)
    }

    private val addFriendUseCase by lazy {
        AddFriendUseCase(friendRepository)
    }

    // Usamos uma ViewModel Factory para poder passar os nossos UseCases para o ViewModel.
    private val friendViewModelFactory by lazy {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(FriendViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return FriendViewModel(getActiveFriendsUseCase, addFriendUseCase) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    // Obtemos a instância do ViewModel que sobreviverá às mudanças de configuração.
    private val viewModel: FriendViewModel by viewModels { friendViewModelFactory }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplitUpTheme {
                // Chamamos o nosso ecrã principal, passando o ViewModel que preparámos.
                FriendScreen(viewModel = viewModel)
            }
        }
    }
}