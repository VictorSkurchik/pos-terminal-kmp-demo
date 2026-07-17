package by.vsdev.posterminal.demo.core.data.di

import by.vsdev.posterminal.demo.domain.usecase.mdm.EnrollDeviceUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.EnrollWithTokenUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.LogoutUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.ObserveEnrollmentUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.ObserveKioskStateUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.SyncDeviceUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.SyncNowUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * MDM/enrollment use-case bindings. Each is a thin, stateless collaborator over the repository and
 * service interfaces, so it's a `factory`. Kiosk/scheduler/executor collaborators are provided by
 * :feature:mdm's module and resolved here by interface.
 */
val domainModule: Module = module {
    factory { ObserveEnrollmentUseCase(get()) }
    factory { ObserveKioskStateUseCase(get()) }
    factory { EnrollDeviceUseCase(get(), get(), get()) }
    factory { EnrollWithTokenUseCase(get(), get(), get(), get()) }
    factory { SyncNowUseCase(get()) }
    factory { LogoutUseCase(get(), get()) }
    factory { SyncDeviceUseCase(get(), get()) }
}
