package com.thaicrew.splitup.check.data

import com.thaicrew.splitup.check.domain.Check
import com.thaicrew.splitup.check.domain.CheckRepository
import com.thaicrew.splitup.check.domain.CheckStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CheckRepositoryImpl(
    private val dao: CheckDao
) : CheckRepository {

    override fun getAllChecks(): Flow<List<Check>> {
        return dao.getAllChecks().map { entities ->
            entities.toDomain()
        }
    }

    override fun getChecksByStatus(status: CheckStatus): Flow<List<Check>> {
        return dao.getChecksByStatus(status.name).map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun getCheckById(id: Int): Check? {
        return dao.getCheckById(id)?.toDomain()
    }

    override suspend fun saveCheck(check: Check): Long {
        val entity = check.toEntity()

        return if (check.id == 0) {
            // Se for novo (ID 0), insere e retorna o NOVO ID gerado pelo banco
            dao.insertCheck(entity)
        } else {
            // Se já existe, atualiza
            dao.updateCheck(entity)
            // Retorna o ID que já existia no objeto
            check.id.toLong()
        }
    }

    override suspend fun deleteCheck(check: Check) {
        dao.deleteCheck(check.toEntity())
    }

    override suspend fun addParticipant(checkId: Int, friendId: Int) {
        val join = FriendParticipateCheckEntity(
            checkId = checkId,
            friendId = friendId
        )
        dao.insertParticipant(join)
    }

    override suspend fun removeParticipant(checkId: Int, friendId: Int) {
        dao.removeParticipant(checkId, friendId)
    }
}