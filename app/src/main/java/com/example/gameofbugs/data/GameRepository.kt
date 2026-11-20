package com.example.gameofbugs.data

import com.example.gameofbugs.model.GameResult
import com.example.gameofbugs.model.GameSettings
import com.example.gameofbugs.model.Player
import com.example.gameofbugs.model.displayName
import com.example.gameofbugs.model.signature

class GameRepository(
    private val userDao: UserDao,
    private val gameRecordDao: GameRecordDao,
    private val activeUserPreferences: ActiveUserPreferences
) {

    suspend fun loadUsers(): List<UserEntity> {
        return userDao.getAll()
    }

    suspend fun loadActiveUser(): UserEntity? {
        val userId = activeUserPreferences.loadActiveUserId() ?: return null
        return userDao.getById(userId)
    }

    fun setActiveUser(userId: Long?) {
        activeUserPreferences.saveActiveUserId(userId)
    }

    suspend fun saveUser(player: Player, selectedUserId: Long?): UserEntity {
        val signature = player.signature()
        val existing = userDao.getBySignature(signature)
            ?: selectedUserId?.let { userDao.getById(it) }

        val user = UserEntity(
            id = existing?.id ?: 0L,
            signature = signature,
            displayName = player.displayName(),
            firstName = player.firstName.trim(),
            lastName = player.lastName.trim(),
            middleName = player.middleName.trim(),
            gender = player.gender,
            birthDate = player.birthDate,
            zodiac = player.zodiac,
            course = player.course,
            difficulty = player.difficulty,
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )

        val savedUser = if (existing == null) {
            user.copy(id = userDao.insert(user))
        } else {
            userDao.update(user)
            user
        }

        setActiveUser(savedUser.id)
        return savedUser
    }

    suspend fun loadRecords(): List<RecordItem> {
        return gameRecordDao.getAll()
    }

    suspend fun saveBestRecord(user: UserEntity, result: GameResult, settings: GameSettings): Boolean {
        val existing = gameRecordDao.getByUserId(user.id)

        val isBetter = when {
            existing == null -> true
            result.score > existing.bestScore -> true
            result.score == existing.bestScore && result.misses < existing.misses -> true
            else -> false
        }

        if (!isBetter) {
            return false
        }

        val record = GameRecordEntity(
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
            gameRecordDao.insert(record)
        } else {
            gameRecordDao.update(record)
        }
        return true
    }
}
