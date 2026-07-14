package by.vsdev.posterminal.demo

import android.app.Application
import by.vsdev.posterminal.demo.core.data.di.dataModule
import by.vsdev.posterminal.demo.core.data.repo.CartRepository
import by.vsdev.posterminal.demo.feature.mdm.di.mdmModule
import by.vsdev.posterminal.demo.feature.pos.di.posModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class PosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val koin = startKoin {
            androidContext(this@PosApplication)
            workManagerFactory()
            modules(dataModule, posModule, mdmModule)
        }.koin

        // Each app start begins with an empty cart (the cart is persisted only within a session).
        CoroutineScope(Dispatchers.IO).launch { koin.get<CartRepository>().clear() }
    }
}
