package com.example.gameofbugs.viewmodel

import androidx.lifecycle.ViewModel
import com.example.gameofbugs.data.GamePreferences
import com.example.gameofbugs.model.GameSettings

class SettingsViewModel(
    private val gamePreferences: GamePreferences
) : ViewModel() {

    fun load(): GameSettings {
        return gamePreferences.load()
    }

    fun save(settings: GameSettings) {
        gamePreferences.save(settings)
    }
}
