package by.vsdev.posterminal.demo.core.data.di

import by.vsdev.posterminal.demo.core.data.enrollment.EnrollmentTokenParserImpl
import by.vsdev.posterminal.demo.core.data.platform.AndroidDeviceInfoProvider
import by.vsdev.posterminal.demo.core.data.platform.DeviceInfoProvider
import by.vsdev.posterminal.demo.core.data.platform.SystemTimeProvider
import by.vsdev.posterminal.demo.core.data.platform.TimeProvider
import by.vsdev.posterminal.demo.core.data.prefs.SettingsRepositoryImpl
import by.vsdev.posterminal.demo.core.data.repo.DeviceRepositoryImpl
import by.vsdev.posterminal.demo.domain.repository.DeviceRepository
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
 * Shared data-layer bindings used by the MDM feature and the app: enrollment settings, the Ktor
 * client, platform providers and the device repository. Feature-specific data (e.g. the POS cart
 * store) lives in the owning feature module.
 */
val dataModule: Module = module {
    single<SettingsRepository> {
        SettingsRepositoryImpl(androidContext(), defaultServerUrl = getProperty("SERVER_URL"))
    }

    // Network — base URL follows the (possibly QR-scanned) serverUrl at call time.
    single<PosApi> { KtorPosApiClient(baseUrlProvider = { get<SettingsRepository>().serverUrl() }) }

    // Platform providers (injectable for tests)
    single<DeviceInfoProvider> { AndroidDeviceInfoProvider() }
    single<TimeProvider> { SystemTimeProvider() }
    single<DispatcherProvider> { DefaultDispatcherProvider() }

    single<DeviceRepository> { DeviceRepositoryImpl(get(), get(), get(), get(), get()) }
    single<EnrollmentTokenParser> { EnrollmentTokenParserImpl() }
}
