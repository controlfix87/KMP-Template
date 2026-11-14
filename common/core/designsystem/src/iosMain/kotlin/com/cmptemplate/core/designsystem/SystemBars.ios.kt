package com.cmptemplate.core.designsystem

import androidx.compose.runtime.Composable

/** The SwiftUI host draws no bars of its own; the status bar follows the system. */
@Composable
internal actual fun SystemBarsEffect(darkTheme: Boolean) = Unit
