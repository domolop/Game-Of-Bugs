package com.example.gameofbugs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface GameRecordDao {
    @Query(
        """
        SELECT 
            r.userId AS userId,
            u.displayName AS displayName,
            r.bestScore AS bestScore,
            r.misses AS misses,
            r.difficulty AS difficulty,
            r.course AS course,
            r.gameSpeed AS gameSpeed,
            r.maxBugs AS maxBugs,
            r.roundDurationSeconds AS roundDurationSeconds,
            r.finishedAt AS finishedAt
        FROM game_records r
        INNER JOIN users u ON u.id = r.userId
        ORDER BY r.bestScore DESC, r.misses ASC, r.finishedAt DESC
        """
    )
    fun getAll(): List<RecordItem>

    @Query("SELECT * FROM game_records WHERE userId = :userId LIMIT 1")
    fun getByUserId(userId: Long): GameRecordEntity?

    @Insert
    fun insert(record: GameRecordEntity): Long

    @Update
    fun update(record: GameRecordEntity)
}
