package com.example.gameofbugs

data class GameSettings(
    val gameSpeed: Int = 5,
    val maxBugs: Int = 10,
    val bonusIntervalSeconds: Int = 15,
    val roundDurationSeconds: Int = 60
) {
    fun sanitized(): GameSettings = copy(
        gameSpeed = gameSpeed.coerceIn(1, 10),
        maxBugs = maxBugs.coerceIn(1, 20),
        bonusIntervalSeconds = bonusIntervalSeconds.coerceIn(5, 60),
        roundDurationSeconds = roundDurationSeconds.coerceIn(30, 300)
    )
}