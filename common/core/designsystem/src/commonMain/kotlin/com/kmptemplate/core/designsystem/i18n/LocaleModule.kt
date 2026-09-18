package com.kmptemplate.core.designsystem.i18n

import org.koin.core.module.Module

/**
 * Provides [LocaleStore] and [LocaleManager]. Add it to the app's module list (see
 * `common/sharedApp/App.kt`) — the persistence behind it is platform-specific, so this has to be a
 * function rather than a `val`.
 */
expect fun localeModule(): Module
