package com.example.gameofbugs

data class GameResult(
    val score: Int,
    val misses: Int,
    val finishedAt: Long = System.currentTimeMillis()
)
