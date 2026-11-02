package com.example.gameofbugs

data class RecordItem(
    val userId: Long,
    val displayName: String,
    val bestScore: Int,
    val misses: Int,
    val difficulty: Int,
    val course: String,
    val gameSpeed: Int,
    val maxBugs: Int,
    val roundDurationSeconds: Int,
    val finishedAt: Long
)
