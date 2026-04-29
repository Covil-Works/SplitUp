package com.thaicrew.splitup.debug

import android.app.Application
import android.content.Context
import androidx.room.withTransaction
import com.thaicrew.splitup.check.data.CheckDao
import com.thaicrew.splitup.check.data.CheckEntity
import com.thaicrew.splitup.check.data.FriendParticipateCheckEntity
import com.thaicrew.splitup.check.data.FriendShareItemEntity
import com.thaicrew.splitup.check.data.ItemDao
import com.thaicrew.splitup.check.data.ItemEntity
import com.thaicrew.splitup.check.domain.CheckStatus
import com.thaicrew.splitup.common.data.local.AppDatabase
import com.thaicrew.splitup.friend.data.FriendDao
import com.thaicrew.splitup.friend.data.FriendEntity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * DEBUG ONLY.
 * This file lives in src/debug and can be removed without touching production logic.
 */
object DebugDatabaseSeeder {
    private const val PREFS_NAME = "debug_database_tools"
    private const val KEY_SEEDED = "seed_v1_applied"
    private const val DAY_IN_MILLIS = 24 * 60 * 60 * 1000L

    @JvmStatic
    fun seedIfNeeded(application: Application) {
        val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) return

        val entryPoint = EntryPointAccessors.fromApplication(
            application,
            DebugDatabaseSeedEntryPoint::class.java
        )

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            runCatching {
                entryPoint.appDatabase().withTransaction {
                    seedDatabase(
                        friendDao = entryPoint.friendDao(),
                        checkDao = entryPoint.checkDao(),
                        itemDao = entryPoint.itemDao()
                    )
                }
            }.onSuccess {
                prefs.edit().putBoolean(KEY_SEEDED, true).apply()
                Timber.d("Debug seed loaded.")
            }.onFailure { throwable ->
                Timber.e(throwable, "Failed to load debug seed.")
            }
        }
    }

    private suspend fun seedDatabase(
        friendDao: FriendDao,
        checkDao: CheckDao,
        itemDao: ItemDao
    ) {
        val friendIds = insertFriends(friendDao)
        val now = System.currentTimeMillis()

        val openHappyHourId = checkDao.insertCheck(
            CheckEntity(
                name = "Happy Hour de Sexta",
                creationDate = now - DAY_IN_MILLIS,
                status = CheckStatus.OPEN.name
            )
        ).toInt()

        val openAlmocoId = checkDao.insertCheck(
            CheckEntity(
                name = "Almoco no Centro",
                creationDate = now - (2 * DAY_IN_MILLIS),
                status = CheckStatus.OPEN.name
            )
        ).toInt()

        val paidChurrascoId = checkDao.insertCheck(
            CheckEntity(
                name = "Churrasco de Abril",
                creationDate = now - (12 * DAY_IN_MILLIS),
                closingDate = now - (10 * DAY_IN_MILLIS),
                status = CheckStatus.PAID.name
            )
        ).toInt()

        val paidCafeId = checkDao.insertCheck(
            CheckEntity(
                name = "Cafe da Reuniao",
                creationDate = now - (7 * DAY_IN_MILLIS),
                closingDate = now - (6 * DAY_IN_MILLIS),
                status = CheckStatus.PAID.name
            )
        ).toInt()

        addParticipants(
            checkDao = checkDao,
            checkId = openHappyHourId,
            friendIds = listOf(
                friendIds.getValue("Ana"),
                friendIds.getValue("Bruno"),
                friendIds.getValue("Carla")
            )
        )

        addParticipants(
            checkDao = checkDao,
            checkId = openAlmocoId,
            friendIds = listOf(
                friendIds.getValue("Bruno"),
                friendIds.getValue("Diego"),
                friendIds.getValue("Elisa")
            )
        )

        addParticipants(
            checkDao = checkDao,
            checkId = paidChurrascoId,
            friendIds = listOf(
                friendIds.getValue("Ana"),
                friendIds.getValue("Diego"),
                friendIds.getValue("Elisa")
            )
        )

        addParticipants(
            checkDao = checkDao,
            checkId = paidCafeId,
            friendIds = listOf(
                friendIds.getValue("Bruno"),
                friendIds.getValue("Carla")
            )
        )

        insertItemWithSharers(
            itemDao = itemDao,
            checkId = openHappyHourId,
            name = "Batata frita",
            quantity = 2,
            valueInCents = 3500,
            sharerIds = listOf(friendIds.getValue("Ana"), friendIds.getValue("Bruno"))
        )

        insertItemWithSharers(
            itemDao = itemDao,
            checkId = openHappyHourId,
            name = "Cerveja",
            quantity = 6,
            valueInCents = 1200,
            sharerIds = listOf(
                friendIds.getValue("Ana"),
                friendIds.getValue("Bruno"),
                friendIds.getValue("Carla")
            )
        )

        insertItemWithSharers(
            itemDao = itemDao,
            checkId = openAlmocoId,
            name = "PF executivo",
            quantity = 3,
            valueInCents = 2890,
            sharerIds = listOf(
                friendIds.getValue("Bruno"),
                friendIds.getValue("Diego"),
                friendIds.getValue("Elisa")
            )
        )

        insertItemWithSharers(
            itemDao = itemDao,
            checkId = openAlmocoId,
            name = "Suco natural",
            quantity = 3,
            valueInCents = 900,
            sharerIds = listOf(friendIds.getValue("Bruno"), friendIds.getValue("Elisa"))
        )

        insertItemWithSharers(
            itemDao = itemDao,
            checkId = paidChurrascoId,
            name = "Picanha",
            quantity = 2,
            valueInCents = 8990,
            sharerIds = listOf(
                friendIds.getValue("Ana"),
                friendIds.getValue("Diego"),
                friendIds.getValue("Elisa")
            )
        )

        insertItemWithSharers(
            itemDao = itemDao,
            checkId = paidChurrascoId,
            name = "Refrigerante",
            quantity = 4,
            valueInCents = 850,
            sharerIds = listOf(friendIds.getValue("Ana"), friendIds.getValue("Diego"))
        )

        insertItemWithSharers(
            itemDao = itemDao,
            checkId = paidCafeId,
            name = "Cappuccino",
            quantity = 2,
            valueInCents = 1450,
            sharerIds = listOf(friendIds.getValue("Bruno"), friendIds.getValue("Carla"))
        )

        insertItemWithSharers(
            itemDao = itemDao,
            checkId = paidCafeId,
            name = "Pao de queijo",
            quantity = 2,
            valueInCents = 700,
            sharerIds = listOf(friendIds.getValue("Bruno"), friendIds.getValue("Carla"))
        )
    }

    private suspend fun insertFriends(friendDao: FriendDao): Map<String, Int> {
        val names = listOf("Ana", "Bruno", "Carla", "Diego", "Elisa")
        return names.associateWith { name ->
            friendDao.insert(
                FriendEntity(
                    name = name,
                    isActive = true
                )
            ).toInt()
        }
    }

    private suspend fun addParticipants(checkDao: CheckDao, checkId: Int, friendIds: List<Int>) {
        checkDao.insertParticipants(
            friendIds.map { friendId ->
                FriendParticipateCheckEntity(
                    friendId = friendId,
                    checkId = checkId
                )
            }
        )
    }

    private suspend fun insertItemWithSharers(
        itemDao: ItemDao,
        checkId: Int,
        name: String,
        quantity: Int,
        valueInCents: Long,
        sharerIds: List<Int>
    ) {
        val itemId = itemDao.insertItem(
            ItemEntity(
                checkId = checkId,
                name = name,
                quantity = quantity,
                valueInCents = valueInCents
            )
        ).toInt()

        sharerIds.forEach { friendId ->
            itemDao.insertItemShare(
                FriendShareItemEntity(
                    friendId = friendId,
                    itemId = itemId,
                    checkId = checkId
                )
            )
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DebugDatabaseSeedEntryPoint {
    fun appDatabase(): AppDatabase
    fun friendDao(): FriendDao
    fun checkDao(): CheckDao
    fun itemDao(): ItemDao
}
