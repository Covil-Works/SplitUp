package com.thaicrew.splitup.friend.data

import com.thaicrew.splitup.friend.domain.Friend
import com.thaicrew.splitup.friend.domain.FriendRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class FriendRepositoryImpl @Inject constructor(
    private val dao: FriendDao
) : FriendRepository {
    override fun getActiveFriends(): Flow<List<Friend>> {
        Timber.d("Solicitando fluxo de amigos ativos do DAO.")
        return dao.getActiveFriends().map { friendEntities ->
            Timber.d("Mapeando ${friendEntities.size} entidades de amigo para o domínio.")
            friendEntities.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun findFriendByName(name: String): Friend? {
        Timber.d("Buscando amigo por nome no DAO: '$name'")
        return dao.findByName(name)?.toDomain()
    }

    override suspend fun findFriendById(id: Int): Friend? {
        Timber.d("Buscando amigo por ID no DAO: $id")
        return dao.findById(id)?.toDomain()
    }

    override suspend fun addFriend(friend: Friend) {
        Timber.d("Mapeando e inserindo o amigo '${friend.name}' no DAO.")
        dao.insert(friend.toEntity())
    }

    override suspend fun updateFriend(friend: Friend) {
        Timber.d("Mapeando e atualizando o amigo '${friend.name}' (ID: ${friend.id}) no DAO.")
        dao.update(friend.toEntity())
    }

    override suspend fun isFriendActive(id: Int): Boolean {
        Timber.d("Verificando no DAO se o amigo com ID $id está ativo.")
        return dao.isFriendActive(id)
    }

    override suspend fun deactivateFriend(id: Int) {
        Timber.d("Solicitando ao DAO a desativação do amigo com ID: $id")
        dao.deactivateFriend(id)
    }

    override suspend fun hardDeleteFriend(id: Int) {
        Timber.d("Executando hard delete para o amigo ID: $id")
        dao.hardDelete(id)
    }

}