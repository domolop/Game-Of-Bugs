package com.example.gameofbugs

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY lastName COLLATE NOCASE, firstName COLLATE NOCASE, middleName COLLATE NOCASE")
    fun getAll(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE signature = :signature LIMIT 1")
    fun getBySignature(signature: String): UserEntity?

    @Insert
    fun insert(user: UserEntity): Long

    @Update
    fun update(user: UserEntity)

    @Delete
    fun delete(user: UserEntity)
}
