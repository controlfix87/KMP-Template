package com.cmptemplate.feature.about.di

import com.cmptemplate.core.network.createHttpClient
import com.cmptemplate.feature.about.ControlFixLinks
import com.cmptemplate.feature.about.report.HttpReportSender
import com.cmptemplate.feature.about.report.ReportSender
import com.cmptemplate.feature.about.report.diagnosticsModule
import com.cmptemplate.feature.about.ui.AboutViewModel
import com.cmptemplate.feature.about.ui.ReportViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose

private val ReportsClient = named("reportsClient")

/** Needs an [com.cmptemplate.core.model.AppInfo] from the host app and `dataStoreModule()`. */
val featureAboutModule = module {
    includes(diagnosticsModule())
    single<HttpClient>(ReportsClient) { createHttpClient(ControlFixLinks.REPORTS_BASE) } onClose { it?.close() }
    single<ReportSender> { HttpReportSender(get(ReportsClient), get(), get()) }
    viewModelOf(::AboutViewModel)
    // ReportKind comes from the route: koinViewModel { parametersOf(kind) }.
    viewModel { params -> ReportViewModel(params.get(), get(), get(), get(), get()) }
}
