package com.example.gameofbugs

import android.content.Context

class ActiveUserPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun loadActiveUserId(): Long? {
        val value = preferences.getLong(KEY_ACTIVE_USER_ID, -1L)
        return value.takeIf { it > 0L }
    }

    fun saveActiveUserId(userId: Long?) {
        preferences.edit().apply {
            if (userId == null) {
                remove(KEY_ACTIVE_USER_ID)
            } else {
                putLong(KEY_ACTIVE_USER_ID, userId)
            }
        }.apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "active_user"
        private const val KEY_ACTIVE_USER_ID = "active_user_id"
    }
}
