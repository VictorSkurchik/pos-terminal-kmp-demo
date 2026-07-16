package by.vsdev.posterminal.demo.core.data.di

import by.vsdev.posterminal.demo.domain.usecase.mdm.EnrollDeviceUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.EnrollWithTokenUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.LogoutUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.ObserveEnrollmentUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.ObserveKioskStateUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.SyncDeviceUseCase
import by.vsdev.posterminal.demo.domain.usecase.mdm.SyncNowUseCase
import by.vsdev.posterminal.demo.domain.usecase.pos.AddToCartUseCase
import by.vsdev.posterminal.demo.domain.usecase.pos.CheckoutUseCase
import by.vsdev.posterminal.demo.domain.usecase.pos.DecrementCartItemUseCase
import by.vsdev.posterminal.demo.domain.usecase.pos.GetProductsUseCase
import by.vsdev.posterminal.demo.domain.usecase.pos.IncrementCartItemUseCase
import by.vsdev.posterminal.demo.domain.usecase.pos.ObserveCartUseCase
import by.vsdev.posterminal.demo.domain.usecase.pos.ObservePaymentRestrictedUseCase
import by.vsdev.posterminal.demo.domain.usecase.pos.RemoveCartItemUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Use-case bindings. Each is a thin, stateless collaborator over the repository interfaces, so it's
 * a `factory`. Kiosk/scheduler/executor collaborators are provided by :feature:mdm's module and
 * resolved here by interface.
 */
val domainModule: Module = module {
    // POS
    factory { ObserveCartUseCase(get()) }
    factory { ObservePaymentRestrictedUseCase(get()) }
    factory { GetProductsUseCase(get()) }
    factory { AddToCartUseCase(get()) }
    factory { IncrementCartItemUseCase(get(), get()) }
    factory { DecrementCartItemUseCase(get()) }
    factory { RemoveCartItemUseCase(get()) }
    factory { CheckoutUseCase(get()) }

    // MDM / enrollment
    factory { ObserveEnrollmentUseCase(get()) }
    factory { ObserveKioskStateUseCase(get()) }
    factory { EnrollDeviceUseCase(get(), get(), get()) }
    factory { EnrollWithTokenUseCase(get(), get(), get(), get()) }
    factory { SyncNowUseCase(get()) }
    factory { LogoutUseCase(get(), get()) }
    factory { SyncDeviceUseCase(get(), get()) }
}
