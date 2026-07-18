package by.vsdev.posterminal.demo.di

import by.vsdev.posterminal.demo.AppViewModel
import by.vsdev.posterminal.demo.domain.policy.DevicePolicy
import by.vsdev.posterminal.demo.policy.DevicePolicyStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule: Module = module {
    // Cross-cutting MDM policy state (kiosk), owned by the app composition root.
    single<DevicePolicy> { DevicePolicyStore(androidContext()) }
    viewModel { AppViewModel(get(), get(), get()) }
}
