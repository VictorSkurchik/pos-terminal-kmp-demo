package by.vsdev.posterminal.demo.core.data.di

import by.vsdev.posterminal.demo.core.data.catalog.ProductRepositoryImpl
import by.vsdev.posterminal.demo.core.data.enrollment.EnrollmentTokenParserImpl
import by.vsdev.posterminal.demo.core.data.local.LocalDatabase
import by.vsdev.posterminal.demo.core.data.local.createLocalDatabase
import by.vsdev.posterminal.demo.core.data.platform.AndroidDeviceInfoProvider
import by.vsdev.posterminal.demo.core.data.platform.DeviceInfoProvider
import by.vsdev.posterminal.demo.core.data.platform.SystemTimeProvider
import by.vsdev.posterminal.demo.core.data.platform.TimeProvider
import by.vsdev.posterminal.demo.core.data.prefs.SettingsRepositoryImpl
import by.vsdev.posterminal.demo.core.data.repo.CartRepositoryImpl
import by.vsdev.posterminal.demo.core.data.repo.DeviceRepositoryImpl
import by.vsdev.posterminal.demo.domain.repository.CartRepository
import by.vsdev.posterminal.demo.domain.repository.DeviceRepository
import by.vsdev.posterminal.demo.domain.repository.ProductRepository
import by.vsdev.posterminal.demo.domain.repository.SettingsRepository
import by.vsdev.posterminal.demo.domain.service.EnrollmentTokenParser
import by.vsdev.posterminal.demo.domain.util.DefaultDispatcherProvider
import by.vsdev.posterminal.demo.domain.util.DispatcherProvider
import by.vsdev.posterminal.demo.network.KtorPosApiClient
import by.vsdev.posterminal.demo.network.PosApi
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Data-layer bindings: each concrete implementation is bound to its domain interface, so features
 * depend only on abstractions. The backend base URL is resolved per-request from settings (which
 * default to the flavor's BuildConfig.SERVER_URL, injected via the "SERVER_URL" Koin property).
 */
val dataModule: Module = module {
    // Persistence
    single { createLocalDatabase(androidContext()) }
    single { get<LocalDatabase>().cartDao() }
    single<SettingsRepository> {
        SettingsRepositoryImpl(androidContext(), defaultServerUrl = getProperty("SERVER_URL"))
    }

    // Network — base URL follows the (possibly QR-scanned) serverUrl at call time.
    single<PosApi> { KtorPosApiClient(baseUrlProvider = { get<SettingsRepository>().serverUrl() }) }

    // Platform providers (injectable for tests)
    single<DeviceInfoProvider> { AndroidDeviceInfoProvider() }
    single<TimeProvider> { SystemTimeProvider() }
    single<DispatcherProvider> { DefaultDispatcherProvider() }

    // Repositories
    single<ProductRepository> { ProductRepositoryImpl() }
    single<CartRepository> { CartRepositoryImpl(get()) }
    single<DeviceRepository> { DeviceRepositoryImpl(get(), get(), get(), get()) }
    single<EnrollmentTokenParser> { EnrollmentTokenParserImpl() }
}
