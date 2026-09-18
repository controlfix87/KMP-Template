package com.kmptemplate.app

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kmptemplate.core.designsystem.i18n.AndroidAppLocale

class MainActivity : ComponentActivity() {

    /**
     * Forces this Activity's resources onto the chosen language, so `resources.getString`, date and
     * number formatting -- anything reading `context.resources` rather than Compose's
     * `stringResource()` -- agrees with the UI from the very first frame.
     * [KMPTemplateApplication] does the same for the Application context, which is the one Android
     * re-derives the process locale from.
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AndroidAppLocale.wrap(newBase))
    }

    /**
     * Every configuration change makes Android re-derive the locale from the device configuration,
     * overriding the in-app choice. Patch the incoming config *before* `super` so the framework
     * propagates the corrected locale to Compose, which reads it via `LocalConfiguration`.
     *
     * This Activity does not declare `android:configChanges`, so normal recreation is preserved (an
     * explicit template rule) and `attachBaseContext` re-applies the locale on the way back up; the
     * patch here covers the changes the platform delivers without a recreation.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        AndroidAppLocale.patch(newConfig, this)
        super.onConfigurationChanged(newConfig)
    }

    /** Repairs the process locale after anything that reset it while this Activity was not resumed. */
    override fun onResume() {
        super.onResume()
        AndroidAppLocale.reassert(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { App() }
    }
}
