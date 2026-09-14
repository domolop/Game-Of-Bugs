package com.example.gameofbugs

data class Player(
    val firstName: String,     // Имя
    val lastName: String,      // Фамилия
    val middleName: String,    // Отчество
    val gender: String,        // Пол: "Мужской" / "Женский"
    val birthDate: String,     // дд.мм.гггг
    val zodiac: String,        // Знак зодиака
    val course: String,        // Курс
    val difficulty: Int        // Сложность
)
