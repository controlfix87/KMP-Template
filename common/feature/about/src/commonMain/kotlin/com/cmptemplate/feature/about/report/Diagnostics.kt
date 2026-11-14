package com.cmptemplate.feature.about.report

import org.koin.core.module.Module

/** A crash recorded by the platform crash recorder on the previous run. */
data class CrashRecord(val atEpochMillis: Long, val trace: String)

/** Platform facts a report carries. Implementations never throw; missing data is null/blank. */
interface Diagnostics {
    fun device(): DeviceInfo
    /** This process's own recent log, unredacted (the sender redacts). Null where unavailable. */
    suspend fun recentLog(): String?
    fun lastCrash(): CrashRecord?
    fun clearCrash()
}

/** Diagnostics with nothing to report, for tests and previews. */
object NoDiagnostics : Diagnostics {
    override fun device() = DeviceInfo()
    override suspend fun recentLog(): String? = null
    override fun lastCrash(): CrashRecord? = null
    override fun clearCrash() = Unit
}

/** Android: logcat + crash file; desktop: crash file; iOS: device details only. */
expect fun diagnosticsModule(): Module
