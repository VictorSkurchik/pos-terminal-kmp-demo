package by.vsdev.posterminal.demo.feature.pos.di

import by.vsdev.posterminal.demo.feature.pos.PosViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val posModule: Module = module {
    viewModel { PosViewModel(get(), get()) }
}
