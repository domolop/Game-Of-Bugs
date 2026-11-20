package com.example.gameofbugs.data

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GoldRepository(
    private val api: GoldApi,
    context: Context
) {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    suspend fun loadRate(): Double? {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val calendar = Calendar.getInstance()
        val to = format.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val from = format.format(calendar.time)

        val rate = try {
            parseRate(api.getMetall(from, to))
        } catch (e: Exception) {
            null
        }

        if (rate == null) {
            return loadSavedRate()
        }
        preferences.edit().putFloat(KEY_RATE, rate.toFloat()).apply()
        return rate
    }

    private fun loadSavedRate(): Double? {
        if (!preferences.contains(KEY_RATE)) {
            return null
        }
        return preferences.getFloat(KEY_RATE, 0f).toDouble()
    }

    private fun parseRate(xml: String): Double? {
        val match = RATE_REGEX.findAll(xml).lastOrNull() ?: return null
        val text = match.groupValues[1]
            .filter { it.isDigit() || it == ',' }
            .replace(',', '.')
        return text.toDoubleOrNull()
    }

    companion object {
        private const val PREFERENCES_NAME = "gold_rate"
        private const val KEY_RATE = "rate"
        private val RATE_REGEX =
            Regex("""<Record[^>]*Code="1"[^>]*>\s*<Buy>([^<]*)</Buy>""")
    }
}
