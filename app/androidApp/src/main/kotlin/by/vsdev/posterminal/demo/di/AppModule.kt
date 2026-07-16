package by.vsdev.posterminal.demo.di

import by.vsdev.posterminal.demo.AppViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule: Module = module {
    viewModel { AppViewModel(get(), get()) }
}
