package com.kmptemplate.app

import android.app.Application
import com.kmptemplate.app.di.coreNetworkModule
import com.kmptemplate.feature.example.di.featureExampleModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class KMPTemplateApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@KMPTemplateApplication)
            modules(
                coreNetworkModule(baseUrl = BuildConfig.API_BASE_URL, enableLogging = BuildConfig.DEBUG),
                featureExampleModule,
                // Register each new feature's di module here, in the same
                // order KoinModulesTest.checkModules() expects -- see that
                // test's doc comment before reordering.
            )
        }
    }
}
