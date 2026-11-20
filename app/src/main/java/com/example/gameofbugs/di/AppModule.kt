package com.example.gameofbugs.di

import androidx.room.Room
import com.example.gameofbugs.data.ActiveUserPreferences
import com.example.gameofbugs.data.AppDatabase
import com.example.gameofbugs.data.GamePreferences
import com.example.gameofbugs.data.GameRepository
import com.example.gameofbugs.data.GoldApi
import com.example.gameofbugs.data.GoldRepository
import com.example.gameofbugs.viewmodel.GameViewModel
import com.example.gameofbugs.viewmodel.RecordsViewModel
import com.example.gameofbugs.viewmodel.RegistrationViewModel
import com.example.gameofbugs.viewmodel.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "game_of_bugs.db").build()
    }
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().gameRecordDao() }

    single {
        Retrofit.Builder()
            .baseUrl("https://www.cbr.ru/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(GoldApi::class.java)
    }

    single { ActiveUserPreferences(androidContext()) }
    single { GamePreferences(androidContext()) }
    single { GameRepository(get(), get(), get()) }
    single { GoldRepository(get(), androidContext()) }

    viewModel { GameViewModel(get(), get(), get()) }
    viewModel { RegistrationViewModel(get()) }
    viewModel { RecordsViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
}
