package by.vsdev.posterminal.demo.core.data.di

import by.vsdev.posterminal.demo.core.data.local.LocalDatabase
import by.vsdev.posterminal.demo.core.data.local.createLocalDatabase
import by.vsdev.posterminal.demo.core.data.prefs.SettingsRepository
import by.vsdev.posterminal.demo.core.data.repo.CartRepository
import by.vsdev.posterminal.demo.core.data.repo.RemoteRepository
import by.vsdev.posterminal.demo.network.PosApiClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/** Backend base URL (Render). For local dev, point `adb reverse` and change this to http://localhost:8080. */
const val DEFAULT_BASE_URL = "https://pos-terminal-kmp-demo.onrender.com"

val dataModule: Module = module {
    single { createLocalDatabase(androidContext()) }
    single { get<LocalDatabase>().cartDao() }
    single { SettingsRepository(androidContext()) }
    single { PosApiClient(baseUrl = DEFAULT_BASE_URL) }
    single { CartRepository(get()) }
    single { RemoteRepository(get(), get()) }
}
