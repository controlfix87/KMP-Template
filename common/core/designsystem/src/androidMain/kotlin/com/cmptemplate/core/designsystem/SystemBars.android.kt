package com.cmptemplate.core.designsystem

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect

@Composable
internal actual fun SystemBarsEffect(darkTheme: Boolean) {
    val activity = LocalActivity.current as? ComponentActivity ?: return
    SideEffect {
        val transparent = android.graphics.Color.TRANSPARENT
        val style = if (darkTheme) SystemBarStyle.dark(transparent) else SystemBarStyle.light(transparent, transparent)
        activity.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }
}
