package by.vsdev.posterminal.demo.feature.mdm.di

import by.vsdev.posterminal.demo.feature.mdm.CommandExecutor
import by.vsdev.posterminal.demo.feature.mdm.CommandFeedViewModel
import by.vsdev.posterminal.demo.feature.mdm.MdmController
import by.vsdev.posterminal.demo.feature.mdm.enrollment.EnrollmentViewModel
import by.vsdev.posterminal.demo.feature.mdm.work.MdmScheduler
import by.vsdev.posterminal.demo.feature.mdm.work.MdmSyncWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mdmModule: Module = module {
    single { MdmController() }
    single { MdmScheduler(androidContext()) }
    factory { CommandExecutor(androidContext(), get(), get(), get()) }
    viewModel { EnrollmentViewModel(get(), get(), get(), get()) }
    viewModel { CommandFeedViewModel(get(), get()) }
    worker { MdmSyncWorker(get(), get(), get(), get(), get()) }
}
