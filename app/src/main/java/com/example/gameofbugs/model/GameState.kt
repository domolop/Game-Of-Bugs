package com.example.gameofbugs.model

data class GameState(
    val score: Int = 0,
    val misses: Int = 0,
    val remainingSeconds: Int = 0,
    val running: Boolean = false,
    val paused: Boolean = false,
    val tilting: Boolean = false
)
