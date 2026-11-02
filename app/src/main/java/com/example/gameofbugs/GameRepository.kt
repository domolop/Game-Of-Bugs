package com.example.gameofbugs

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class GameRepository private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val database = AppDatabase.getInstance(appContext)
    private val userPreferences = ActiveUserPreferences(appContext)
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun loadUsers(callback: (List<UserEntity>) -> Unit) {
        executor.execute {
            val users = database.userDao().getAll()
            mainHandler.post { callback(users) }
        }
    }

    fun loadActiveUser(callback: (UserEntity?) -> Unit) {
        executor.execute {
            val userId = userPreferences.loadActiveUserId()
            val user = userId?.let { database.userDao().getById(it) }
            mainHandler.post { callback(user) }
        }
    }

    fun setActiveUser(userId: Long?) {
        userPreferences.saveActiveUserId(userId)
    }

    fun saveUser(player: Player, selectedUserId: Long?, callback: (UserEntity) -> Unit) {
        executor.execute {
            val userDao = database.userDao()
            val signature = player.signature()
            val existingById = selectedUserId?.let { userDao.getById(it) }
            val existingBySignature = userDao.getBySignature(signature)
            val existing = existingBySignature ?: existingById

            val entity = if (existing == null) {
                UserEntity(
                    signature = signature,
                    displayName = player.displayName(),
                    firstName = player.firstName.trim(),
                    lastName = player.lastName.trim(),
                    middleName = player.middleName.trim(),
                    gender = player.gender,
                    birthDate = player.birthDate,
                    zodiac = player.zodiac,
                    course = player.course,
                    difficulty = player.difficulty
                )
            } else {
                existing.copy(
                    signature = signature,
                    displayName = player.displayName(),
                    firstName = player.firstName.trim(),
                    lastName = player.lastName.trim(),
                    middleName = player.middleName.trim(),
                    gender = player.gender,
                    birthDate = player.birthDate,
                    zodiac = player.zodiac,
                    course = player.course,
                    difficulty = player.difficulty
                )
            }

            val saved = if (entity.id == 0L) {
                val insertedId = userDao.insert(entity)
                entity.copy(id = insertedId)
            } else {
                userDao.update(entity)
                entity
            }

            setActiveUser(saved.id)
            mainHandler.post { callback(saved) }
        }
    }

    fun loadRecords(callback: (List<RecordItem>) -> Unit) {
        executor.execute {
            val records = database.gameRecordDao().getAll()
            mainHandler.post { callback(records) }
        }
    }

    fun saveBestRecord(
        user: UserEntity,
        result: GameResult,
        settings: GameSettings,
        callback: (Boolean) -> Unit = {}
    ) {
        executor.execute {
            val dao = database.gameRecordDao()
            val existing = dao.getByUserId(user.id)

            val isBetter = when {
                existing == null -> true
                result.score > existing.bestScore -> true
                result.score == existing.bestScore && result.misses < existing.misses -> true
                else -> false
            }

            if (!isBetter) {
                mainHandler.post { callback(false) }
                return@execute
            }

            val entity = GameRecordEntity(
                recordId = existing?.recordId ?: 0L,
                userId = user.id,
                bestScore = result.score,
                misses = result.misses,
                difficulty = user.difficulty,
                course = user.course,
                gameSpeed = settings.gameSpeed,
                maxBugs = settings.maxBugs,
                roundDurationSeconds = settings.roundDurationSeconds,
                finishedAt = result.finishedAt
            )

            if (existing == null) {
                dao.insert(entity)
            } else {
                dao.update(entity)
            }

            mainHandler.post { callback(true) }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: GameRepository? = null

        fun getInstance(context: Context): GameRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GameRepository(context).also { INSTANCE = it }
            }
        }
    }
}
