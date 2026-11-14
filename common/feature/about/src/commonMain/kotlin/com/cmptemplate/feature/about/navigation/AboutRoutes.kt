package com.cmptemplate.feature.about.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.cmptemplate.feature.about.report.ReportKind
import com.cmptemplate.feature.about.ui.AboutRoot
import com.cmptemplate.feature.about.ui.ReportRoot
import kotlinx.serialization.Serializable

sealed interface AboutRoute : NavKey {
    @Serializable data object About : AboutRoute
    /** [kind] is a [ReportKind.wire] value: a plain string keeps the saved back stack stable. */
    @Serializable data class Report(val kind: String) : AboutRoute
}

/**
 * About and the report screen. [onRunSetupAgain] null hides "Run setup again" (projects generated
 * without the onboarding module).
 */
fun EntryProviderScope<NavKey>.aboutEntries(
    onBack: () -> Unit,
    onReport: (ReportKind) -> Unit,
    onRunSetupAgain: (() -> Unit)?,
) {
    entry<AboutRoute.About> { AboutRoot(onBack, onReport, onRunSetupAgain) }
    entry<AboutRoute.Report> { key -> ReportRoot(ReportKind.ofWire(key.kind), onBack) }
}
