package by.vsdev.posterminal.demo

import android.app.Application
import by.vsdev.posterminal.demo.core.data.di.dataModule
import by.vsdev.posterminal.demo.feature.mdm.di.mdmModule
import by.vsdev.posterminal.demo.feature.pos.di.posModule
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class PosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PosApplication)
            workManagerFactory()
            modules(dataModule, posModule, mdmModule)
        }
    }
}
