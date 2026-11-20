package com.example.gameofbugs.model

data class GameResult(
    val score: Int,
    val misses: Int,
    val finishedAt: Long = System.currentTimeMillis()
)
