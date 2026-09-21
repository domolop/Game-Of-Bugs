package com.example.gameofbugs

import android.content.Context

class GamePreferences(context: Context) {

    private val preferences =
        context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    fun load(): GameSettings {
        return GameSettings(
            gameSpeed =
                preferences.getInt(
                    KEY_GAME_SPEED,
                    5
                ),

            maxBugs =
                preferences.getInt(
                    KEY_MAX_BUGS,
                    10
                ),

            bonusIntervalSeconds =
                preferences.getInt(
                    KEY_BONUS_INTERVAL,
                    15
                ),

            roundDurationSeconds =
                preferences.getInt(
                    KEY_ROUND_DURATION,
                    60
                )
        ).sanitized()
    }

    fun save(settings: GameSettings) {
        val value = settings.sanitized()

        preferences.edit()
            .putInt(
                KEY_GAME_SPEED,
                value.gameSpeed
            )
            .putInt(
                KEY_MAX_BUGS,
                value.maxBugs
            )
            .putInt(
                KEY_BONUS_INTERVAL,
                value.bonusIntervalSeconds
            )
            .putInt(
                KEY_ROUND_DURATION,
                value.roundDurationSeconds
            )
            .apply()
    }

    companion object {
        const val PREFERENCES_NAME = "game_settings"

        const val KEY_GAME_SPEED =
            "game_speed"

        const val KEY_MAX_BUGS =
            "max_bugs"

        const val KEY_BONUS_INTERVAL =
            "bonus_interval"

        const val KEY_ROUND_DURATION =
            "round_duration"
    }
}