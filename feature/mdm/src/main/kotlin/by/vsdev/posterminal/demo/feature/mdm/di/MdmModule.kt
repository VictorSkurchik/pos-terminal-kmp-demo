package by.vsdev.posterminal.demo.feature.mdm.di

import by.vsdev.posterminal.demo.domain.service.DeviceAdminRepository
import by.vsdev.posterminal.demo.domain.service.KioskController
import by.vsdev.posterminal.demo.domain.service.MdmCommandExecutor
import by.vsdev.posterminal.demo.domain.service.MdmScheduler
import by.vsdev.posterminal.demo.feature.mdm.CommandExecutor
import by.vsdev.posterminal.demo.feature.mdm.MdmController
import by.vsdev.posterminal.demo.feature.mdm.admin.DeviceAdminRepositoryImpl
import by.vsdev.posterminal.demo.feature.mdm.enrollment.EnrollmentViewModel
import by.vsdev.posterminal.demo.feature.mdm.work.MdmSyncWorker
import by.vsdev.posterminal.demo.feature.mdm.work.WorkManagerMdmScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val mdmModule: Module = module {
    single { MdmController() } bind KioskController::class
    single<MdmScheduler> { WorkManagerMdmScheduler(androidContext()) }
    single<DeviceAdminRepository> { DeviceAdminRepositoryImpl(androidContext()) }
    single<MdmCommandExecutor> { CommandExecutor(androidContext(), get(), get(), get(), get()) }
    viewModel { EnrollmentViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    worker { MdmSyncWorker(get(), get(), get(), get()) }
}
