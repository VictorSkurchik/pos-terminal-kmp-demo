package by.vsdev.posterminal.demo.feature.mdm.di

import by.vsdev.posterminal.demo.domain.util.DefaultDispatcherProvider
import by.vsdev.posterminal.demo.domain.util.DispatcherProvider
import by.vsdev.posterminal.demo.feature.mdm.CommandExecutor
import by.vsdev.posterminal.demo.feature.mdm.MdmController
import by.vsdev.posterminal.demo.feature.mdm.admin.DeviceAdminRepositoryImpl
import by.vsdev.posterminal.demo.feature.mdm.data.AndroidDeviceInfoProvider
import by.vsdev.posterminal.demo.feature.mdm.data.DeviceInfoProvider
import by.vsdev.posterminal.demo.feature.mdm.data.DeviceRepositoryImpl
import by.vsdev.posterminal.demo.feature.mdm.data.EnrollmentTokenParserImpl
import by.vsdev.posterminal.demo.feature.mdm.data.SettingsRepositoryImpl
import by.vsdev.posterminal.demo.feature.mdm.data.SystemTimeProvider
import by.vsdev.posterminal.demo.feature.mdm.data.TimeProvider
import by.vsdev.posterminal.demo.feature.mdm.domain.repository.DeviceRepository
import by.vsdev.posterminal.demo.feature.mdm.domain.repository.SettingsRepository
import by.vsdev.posterminal.demo.feature.mdm.domain.service.DeviceAdminRepository
import by.vsdev.posterminal.demo.feature.mdm.domain.service.EnrollmentTokenParser
import by.vsdev.posterminal.demo.feature.mdm.domain.service.KioskController
import by.vsdev.posterminal.demo.feature.mdm.domain.service.MdmCommandExecutor
import by.vsdev.posterminal.demo.feature.mdm.domain.service.MdmScheduler
import by.vsdev.posterminal.demo.feature.mdm.domain.usecase.EnrollDeviceUseCase
import by.vsdev.posterminal.demo.feature.mdm.domain.usecase.EnrollWithTokenUseCase
import by.vsdev.posterminal.demo.feature.mdm.domain.usecase.LogoutUseCase
import by.vsdev.posterminal.demo.feature.mdm.domain.usecase.ObserveEnrollmentUseCase
import by.vsdev.posterminal.demo.feature.mdm.domain.usecase.ObserveKioskStateUseCase
import by.vsdev.posterminal.demo.feature.mdm.domain.usecase.SyncDeviceUseCase
import by.vsdev.posterminal.demo.feature.mdm.domain.usecase.SyncNowUseCase
import by.vsdev.posterminal.demo.feature.mdm.enrollment.RegistrationViewModel
import by.vsdev.posterminal.demo.feature.mdm.enrollment.SettingsViewModel
import by.vsdev.posterminal.demo.feature.mdm.work.MdmSyncWorker
import by.vsdev.posterminal.demo.feature.mdm.work.WorkManagerMdmScheduler
import by.vsdev.posterminal.demo.network.KtorPosApiClient
import by.vsdev.posterminal.demo.network.PosApi
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * The MDM feature owns its full stack: enrollment settings + backend transport, device-policy
 * services, use cases, framework infra (service/receiver/worker) and presentation.
 */
val mdmModule: Module = module {
    // Enrollment settings + backend transport (base URL follows the QR-scanned serverUrl at call time).
    single<SettingsRepository> {
        SettingsRepositoryImpl(androidContext(), defaultServerUrl = getProperty("SERVER_URL"))
    }
    single<PosApi> { KtorPosApiClient(baseUrlProvider = { get<SettingsRepository>().serverUrl() }) }

    // Platform providers + dispatchers
    single<DeviceInfoProvider> { AndroidDeviceInfoProvider() }
    single<TimeProvider> { SystemTimeProvider() }
    single<DispatcherProvider> { DefaultDispatcherProvider() }

    // Repositories / services
    single<DeviceRepository> { DeviceRepositoryImpl(get(), get(), get(), get(), get()) }
    single<EnrollmentTokenParser> { EnrollmentTokenParserImpl() }
    single { MdmController() } bind KioskController::class
    single<MdmScheduler> { WorkManagerMdmScheduler(androidContext()) }
    single<DeviceAdminRepository> { DeviceAdminRepositoryImpl(androidContext()) }
    single<MdmCommandExecutor> { CommandExecutor(androidContext(), get(), get(), get(), get()) }

    // Use cases
    factory { ObserveEnrollmentUseCase(get()) }
    factory { ObserveKioskStateUseCase(get()) }
    factory { EnrollDeviceUseCase(get(), get(), get()) }
    factory { EnrollWithTokenUseCase(get(), get(), get(), get()) }
    factory { SyncNowUseCase(get()) }
    factory { LogoutUseCase(get(), get()) }
    factory { SyncDeviceUseCase(get(), get()) }

    // Presentation + infra
    viewModel { RegistrationViewModel(get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get()) }
    worker { MdmSyncWorker(get(), get(), get(), get()) }
}
