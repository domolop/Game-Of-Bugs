package com.example.gameofbugs

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["signature"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val signature: String,
    val displayName: String,
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val gender: String,
    val birthDate: String,
    val zodiac: String,
    val course: String,
    val difficulty: Int,
    val createdAt: Long = System.currentTimeMillis()
)
