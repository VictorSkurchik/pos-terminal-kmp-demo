package by.vsdev.posterminal.demo.feature.offer.di

import by.vsdev.posterminal.demo.feature.offer.OfferViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val offerModule: Module = module {
    viewModel { OfferViewModel() }
}
