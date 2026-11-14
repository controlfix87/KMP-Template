package com.cmptemplate.feature.about.report

import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Locale

/** Crash file in the user's home: `~/.cmptemplate/diagnostics/last-crash.txt`. Install from `main()`. */
object CrashRecorder {
    internal val file: File get() = File(System.getProperty("user.home"), ".cmptemplate/diagnostics/last-crash.txt")

    fun install() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching {
                val trace = StringWriter().also { error.printStackTrace(PrintWriter(it)) }.toString()
                file.apply { parentFile?.mkdirs() }.writeText("Thread: ${thread.name}\n${trace.take(ReportSender.CRASH_MAX)}")
            }
            previous?.uncaughtException(thread, error)
        }
    }
}

internal class DesktopDiagnostics : Diagnostics {
    override fun device() = DeviceInfo(
        manufacturer = "",
        model = System.getProperty("os.arch").orEmpty(),
        device = "JVM " + System.getProperty("java.version").orEmpty(),
        os = "${System.getProperty("os.name").orEmpty()} ${System.getProperty("os.version").orEmpty()}".trim(),
        type = "desktop",
        language = Locale.getDefault().toLanguageTag(),
    )

    /** No process log to read on the desktop; add a file logger here if the product has one. */
    override suspend fun recentLog(): String? = null

    override fun lastCrash(): CrashRecord? = runCatching {
        CrashRecorder.file.takeIf { it.isFile && it.length() > 0 }?.let { CrashRecord(it.lastModified(), it.readText()) }
    }.getOrNull()

    override fun clearCrash() {
        runCatching { CrashRecorder.file.delete() }
    }
}

actual fun diagnosticsModule(): Module = module {
    single<Diagnostics> { DesktopDiagnostics() }
}
