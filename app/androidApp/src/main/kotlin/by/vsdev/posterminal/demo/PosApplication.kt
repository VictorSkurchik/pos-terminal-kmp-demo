package by.vsdev.posterminal.demo

import android.app.Application
import by.vsdev.posterminal.demo.core.data.di.dataModule
import by.vsdev.posterminal.demo.core.data.di.domainModule
import by.vsdev.posterminal.demo.di.appModule
import by.vsdev.posterminal.demo.domain.repository.CartRepository
import by.vsdev.posterminal.demo.feature.mdm.di.mdmModule
import by.vsdev.posterminal.demo.feature.offer.di.offerModule
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
            // Backend default for this flavor; the data layer prefers the QR-scanned serverUrl at runtime.
            properties(mapOf("SERVER_URL" to BuildConfig.SERVER_URL))
            modules(dataModule, domainModule, posModule, mdmModule, offerModule, appModule)
        }.koin

        // Each app start begins with an empty cart (the cart is persisted only within a session).
        CoroutineScope(Dispatchers.IO).launch { koin.get<CartRepository>().clear() }
    }
}
