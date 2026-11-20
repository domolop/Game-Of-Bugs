package com.example.gameofbugs.model

enum class BugType(
    val points: Int,
    val sizeDp: Float,
    val speedMultiplier: Float
) {
    COCKROACH(1, 44f, 1.0f),
    BEETLE(2, 42f, 0.85f),
    SPIDER(3, 38f, 1.15f),
    BUTTERFLY(4, 46f, 1.3f),
    GOLD(0, 46f, 1.2f)
}

data class GameBug(
    val type: BugType,
    val points: Int,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val speed: Float
)
