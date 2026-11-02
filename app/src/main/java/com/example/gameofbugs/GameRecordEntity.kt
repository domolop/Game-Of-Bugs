package com.example.gameofbugs

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "game_records",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"], unique = true)]
)
data class GameRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val recordId: Long = 0,
    val userId: Long,
    val bestScore: Int,
    val misses: Int,
    val difficulty: Int,
    val course: String,
    val gameSpeed: Int,
    val maxBugs: Int,
    val roundDurationSeconds: Int,
    val finishedAt: Long
)
