package com.example.gameofbugs

import java.util.Locale

data class Player(
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val gender: String,
    val birthDate: String,
    val zodiac: String,
    val course: String,
    val difficulty: Int
)

fun Player.displayName(): String {
    return listOf(lastName, firstName, middleName)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(" ")
}

fun Player.signature(): String {
    return listOf(lastName, firstName, middleName, birthDate)
        .map { it.trim().lowercase(Locale.ROOT) }
        .joinToString("|")
}
