package by.vsdev.posterminal.demo.feature.pos.di

import by.vsdev.posterminal.demo.feature.pos.data.CartRepositoryImpl
import by.vsdev.posterminal.demo.feature.pos.data.ProductRepositoryImpl
import by.vsdev.posterminal.demo.feature.pos.data.local.LocalDatabase
import by.vsdev.posterminal.demo.feature.pos.data.local.createLocalDatabase
import by.vsdev.posterminal.demo.feature.pos.domain.repository.CartRepository
import by.vsdev.posterminal.demo.feature.pos.domain.repository.ProductRepository
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.AddToCartUseCase
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.CheckoutUseCase
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.DecrementCartItemUseCase
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.GetProductsUseCase
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.IncrementCartItemUseCase
import by.vsdev.posterminal.demo.feature.pos.domain.usecase.ObserveCartUseCase
import by.vsdev.posterminal.demo.feature.pos.presentation.PosViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** The POS feature owns its full stack: Room cart store, repositories, use cases and ViewModel. */
val posModule: Module = module {
    // Persistence (cart)
    single { createLocalDatabase(androidContext()) }
    single { get<LocalDatabase>().cartDao() }

    // Repositories
    single<ProductRepository> { ProductRepositoryImpl() }
    single<CartRepository> { CartRepositoryImpl(get()) }

    // Use cases
    factory { ObserveCartUseCase(get()) }
    factory { GetProductsUseCase(get()) }
    factory { AddToCartUseCase(get()) }
    factory { IncrementCartItemUseCase(get(), get()) }
    factory { DecrementCartItemUseCase(get()) }
    factory { CheckoutUseCase(get()) }

    viewModel { PosViewModel(get(), get(), get(), get(), get(), get(), get()) }
}
