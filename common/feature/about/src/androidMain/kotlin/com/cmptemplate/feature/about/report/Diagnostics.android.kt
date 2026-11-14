package com.cmptemplate.feature.about.report

import android.content.Context
import android.os.Build
import android.os.Process
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Records an uncaught crash to app-private storage so the next launch can offer to send it: the
 * crashed process and its log are gone by then. Install from `Application.onCreate`, right after
 * `super.onCreate()`. Never throws; always hands on to the previous handler.
 */
object CrashRecorder {
    internal fun file(context: Context) = File(context.applicationContext.filesDir, "diagnostics/last-crash.txt")

    fun install(context: Context) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        val app = context.applicationContext
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching {
                val trace = StringWriter().also { error.printStackTrace(PrintWriter(it)) }.toString()
                file(app).apply { parentFile?.mkdirs() }
                    .writeText("Thread: ${thread.name}\n${trace.take(ReportSender.CRASH_MAX)}")
            }
            previous?.uncaughtException(thread, error)
        }
    }
}

internal class AndroidDiagnostics(private val context: Context) : Diagnostics {
    override fun device() = DeviceInfo(
        manufacturer = Build.MANUFACTURER.orEmpty(),
        model = Build.MODEL.orEmpty(),
        device = Build.DEVICE.orEmpty(),
        os = "Android " + Build.VERSION.RELEASE,
        api = Build.VERSION.SDK_INT.toString(),
        type = if (context.resources.configuration.smallestScreenWidthDp >= 600) "tablet" else "phone",
        language = Locale.getDefault().toLanguageTag(),
    )

    /** This process's own log: no permission needed. Last 1500 lines, at most 5 s. */
    override suspend fun recentLog(): String? = withContext(Dispatchers.IO) {
        runCatching {
            val p = ProcessBuilder("logcat", "-d", "-v", "time", "-t", "1500", "--pid=${Process.myPid()}")
                .redirectErrorStream(true).start()
            val text = p.inputStream.bufferedReader().use { it.readText() }
            p.waitFor(5, TimeUnit.SECONDS)
            text.takeLast(ReportSender.LOG_MAX)
        }.getOrNull()
    }

    override fun lastCrash(): CrashRecord? = runCatching {
        CrashRecorder.file(context).takeIf { it.isFile && it.length() > 0 }?.let { CrashRecord(it.lastModified(), it.readText()) }
    }.getOrNull()

    override fun clearCrash() {
        runCatching { CrashRecorder.file(context).delete() }
    }
}

actual fun diagnosticsModule(): Module = module {
    single<Diagnostics> { AndroidDiagnostics(get()) }
}
