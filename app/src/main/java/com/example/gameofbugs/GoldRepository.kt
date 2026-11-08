package com.example.gameofbugs

import android.content.Context
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GoldRepository private constructor(context: Context) {

    private val preferences = context.applicationContext
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val api = Retrofit.Builder()
        .baseUrl("https://www.cbr.ru/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(GoldApi::class.java)

    fun loadRate(callback: (Double?) -> Unit) {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val calendar = Calendar.getInstance()
        val to = format.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val from = format.format(calendar.time)

        api.getMetall(from, to).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                val rate = parseRate(response.body())
                if (rate == null) {
                    callback(loadSavedRate())
                } else {
                    preferences.edit().putFloat(KEY_RATE, rate.toFloat()).apply()
                    callback(rate)
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                callback(loadSavedRate())
            }
        })
    }

    private fun loadSavedRate(): Double? {
        if (!preferences.contains(KEY_RATE)) {
            return null
        }
        return preferences.getFloat(KEY_RATE, 0f).toDouble()
    }

    private fun parseRate(xml: String?): Double? {
        if (xml == null) {
            return null
        }
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

        @Volatile
        private var INSTANCE: GoldRepository? = null

        fun getInstance(context: Context): GoldRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GoldRepository(context).also { INSTANCE = it }
            }
        }
    }
}
