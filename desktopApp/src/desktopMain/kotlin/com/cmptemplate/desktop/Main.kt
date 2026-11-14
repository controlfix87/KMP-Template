package com.cmptemplate.desktop

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.cmptemplate.app.App
import com.cmptemplate.app.appModules
import com.cmptemplate.core.model.AppInfo
import com.cmptemplate.feature.about.report.CrashRecorder
import org.koin.core.context.startKoin
import org.koin.dsl.module

/** Desktop identity. Keep [AppInfo.id] equal to the Android package so tickets land together. */
const val DESKTOP_VERSION = "1.0.0"

fun desktopHostModule() = module {
    single { AppInfo("com.cmptemplate.app", "CMPTemplate", DESKTOP_VERSION, "1", "desktop", "desktop") }
}

fun main() {
    CrashRecorder.install()
    startKoin { modules(appModules + desktopHostModule()) }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "CMPTemplate",
            state = rememberWindowState(size = DpSize(1024.dp, 760.dp)),
        ) { App() }
    }
}
